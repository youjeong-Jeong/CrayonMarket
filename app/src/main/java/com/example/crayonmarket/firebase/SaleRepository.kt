package com.example.crayonmarket.firebase

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.crayonmarket.firebase.dto.SaleDto
import com.example.crayonmarket.firebase.dto.UserDto
import com.example.crayonmarket.firebase.paging.SalePagingSource
import com.example.crayonmarket.model.Sale
import com.example.crayonmarket.model.SortType
import com.example.crayonmarket.view.common.timeAgoString
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

object SaleRepository {

    const val PAGE_SIZE = 20

    suspend fun getHomeFeeds(sortType: SortType, flags: Boolean): Flow<PagingData<Sale>> {
        try {
            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                SalePagingSource(getWriterUuids = {
                    val result = UserRepository.getAllUserList()
                    if (result.isSuccess) {
                        result.getOrNull()!!.map { it.uuid }.toMutableList()
                    } else {
                        throw IllegalStateException("회원 정보 얻기 실패")
                    }
                }, sortType = sortType, flags = flags)
            }.flow
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun getMyFeeds(): Flow<PagingData<Sale>> {
        val currentUserUuid = AuthRepository.currentUserUuid
        requireNotNull(currentUserUuid)
        try {
            return Pager(PagingConfig(pageSize = PAGE_SIZE)) {
                SalePagingSource(
                    getWriterUuids = { listOf(currentUserUuid) },
                    SortType.LATEST_ORDER,
                    flags = false
                )
            }.flow
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getSaleDetail(saleUuid: String): Result<Sale> {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val userCollection = db.collection("users")
        val saleCollection = db.collection("posts")
        check(currentUser != null)

        return try {
            val saleDto =
                saleCollection.document(saleUuid).get().await().toObject(SaleDto::class.java)!!
            val writer = userCollection.document(saleDto.writerUuid).get().await()
                .toObject(UserDto::class.java)
            Result.success(
                Sale(
                    uuid = saleDto.uuid,
                    title = saleDto.title,
                    writerUuid = writer!!.uuid,
                    writerName = writer.name,
                    writerProfileImageUrl = writer.profileImageUrl,
                    content = saleDto.content,
                    imageUrl = saleDto.imageUrl,
                    isMine = saleDto.writerUuid == currentUser.uid,
                    cost = saleDto.cost,
                    time = saleDto.time.timeAgoString(),
                    possibleSale = saleDto.possibleSale
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun editSaleOnlyPossible(
        uuid: String, possible: Boolean
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val postCollection = db.collection("posts")
        val map = mutableMapOf<String, Any>()

        map["possibleSale"] = possible

        return try {
            postCollection.document(uuid).update(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSale(postUuid: String): Result<Unit> {
        val db = Firebase.firestore

        return try {
            db.collection("posts").document(postUuid).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editSale(
        uuid: String, title: String, content: String, imageUri: Uri, cost: String
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val postCollection = db.collection("posts")
        val imageFileName: String = UUID.randomUUID().toString() + ".png"
        val imageRef = storageRef.child(imageFileName)
        val map = mutableMapOf<String, Any>()

        map["title"] = title
        map["content"] = content
        map["imageUrl"] = imageFileName
        map["cost"] = cost

        try {
            imageRef.putFile(imageUri).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            postCollection.document(uuid).update(map).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun uploadSale(
        title: String, content: String, imageUri: Uri, cost: String
    ): Result<Unit> {
        val currentUser = Firebase.auth.currentUser
        require(currentUser != null)
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference
        val postCollection = db.collection("posts")
        val imageFileName: String = UUID.randomUUID().toString() + ".png"
        val imageRef = storageRef.child(imageFileName)
        val postUuid = UUID.randomUUID().toString()

        try {
            imageRef.putFile(imageUri).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {
            val saleDto = SaleDto(
                uuid = postUuid,
                title = title,
                cost = cost.toLong(),
                writerUuid = currentUser.uid,
                content = content,
                imageUrl = imageFileName,
                time = Date(),
                possibleSale = true
            )
            postCollection.document(postUuid).set(saleDto).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}