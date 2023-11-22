package com.example.crayonmarket.firebase.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.crayonmarket.firebase.SaleRepository
import com.example.crayonmarket.firebase.dto.SaleDto
import com.example.crayonmarket.firebase.dto.UserDto
import com.example.crayonmarket.model.Sale
import com.example.crayonmarket.model.SortType
import com.example.crayonmarket.view.common.timeAgoString
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SalePagingSource(
    private val getWriterUuids: suspend () -> List<String>,
    private val sortType: SortType,
    private val flags: Boolean
) : PagingSource<QuerySnapshot, Sale>() {

    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = Firebase.firestore.collection("users")

    private val tempQueryPosts =
        Firebase.firestore.collection("posts").limit(SaleRepository.PAGE_SIZE.toLong())

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Sale>): QuerySnapshot? {
        return null
    }

    override suspend fun load(
        params: LoadParams<QuerySnapshot>
    ): LoadResult<QuerySnapshot, Sale> {
        val writerUuidList = getWriterUuids()
        var queryPosts = tempQueryPosts

        when (sortType.sortNumber) {
            0 -> queryPosts = tempQueryPosts.orderBy("time", Query.Direction.DESCENDING)
            1 -> queryPosts = tempQueryPosts.orderBy("cost", Query.Direction.DESCENDING)
            2 -> queryPosts = tempQueryPosts.orderBy("cost", Query.Direction.ASCENDING)
        }

        return try {
            val currentPage = params.key ?: queryPosts.get().await()
            if (currentPage.isEmpty) {
                return LoadResult.Page(
                    data = emptyList(), prevKey = null, nextKey = null
                )
            }
            val lastVisiblePost = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryPosts.startAfter(lastVisiblePost).get().await()
            var saleDtos = currentPage.toObjects(SaleDto::class.java)
            if(flags){
                saleDtos = saleDtos.filter { saleDto ->
                    saleDto.possibleSale
                }
            }
            val sales = saleDtos.filter { saleDto ->
                writerUuidList.contains(saleDto.writerUuid)
            }.map { saleDto ->
                val writer = userCollection.document(saleDto.writerUuid).get().await()
                    .toObject(UserDto::class.java)
                Sale(
                    uuid = saleDto.uuid,
                    title = saleDto.title,
                    writerUuid = writer!!.uuid,
                    writerName = writer.name,
                    writerProfileImageUrl = writer.profileImageUrl,
                    content = saleDto.content,
                    imageUrl = saleDto.imageUrl,
                    isMine = saleDto.writerUuid == currentUserId,
                    cost = saleDto.cost,
                    time = saleDto.time.timeAgoString(),
                    possibleSale = saleDto.possibleSale
                )
            }

            LoadResult.Page(
                data = sales, prevKey = null, nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
