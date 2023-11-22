package com.example.crayonmarket.firebase.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.crayonmarket.firebase.ChatRepository.PAGE_SIZE
import com.example.crayonmarket.firebase.dto.ChatRoomDto
import com.example.crayonmarket.firebase.dto.UserDto
import com.example.crayonmarket.model.ChatRoom
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ChatRoomPagingSource : PagingSource<QuerySnapshot, ChatRoom>() {

    private val currentUserId = Firebase.auth.currentUser!!.uid
    private val userCollection = Firebase.firestore.collection("users")

    private val queryRooms =
        Firebase.firestore.collection("rooms").orderBy("time", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())

    override fun getRefreshKey(state: PagingState<QuerySnapshot, ChatRoom>): QuerySnapshot? {

        return null
    }

    override suspend fun load(
        params: LoadParams<QuerySnapshot>
    ): LoadResult<QuerySnapshot, ChatRoom> {

        return try {
            val currentPage = params.key ?: queryRooms.get().await()
            if (currentPage.isEmpty) {
                return LoadResult.Page(
                    data = emptyList(), prevKey = null, nextKey = null
                )
            }

            val lastVisiblePost = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryRooms.startAfter(lastVisiblePost).get().await()
            val chatRoomDtos = currentPage.toObjects(ChatRoomDto::class.java)
            val chatRooms = chatRoomDtos.filter { chatRoomDto ->
                chatRoomDto.conversationWriterUserUid == currentUserId || chatRoomDto.conversationAppliedUserUid == currentUserId
            }.map { chatRoomDto ->
                val userUuid = if (chatRoomDto.conversationAppliedUserUid != currentUserId) {
                    chatRoomDto.conversationAppliedUserUid
                } else {
                    chatRoomDto.conversationWriterUserUid
                }
                val writer =
                    userCollection.document(userUuid).get().await().toObject(UserDto::class.java)
                ChatRoom(
                    uuid = chatRoomDto.uuid,
                    conversationAppliedUserName = writer!!.name,
                    conversationAppliedUserProfileImage = writer.profileImageUrl
                )
            }
            LoadResult.Page(
                data = chatRooms, prevKey = null, nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}