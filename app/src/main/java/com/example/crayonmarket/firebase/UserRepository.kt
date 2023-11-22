package com.example.crayonmarket.firebase

import android.graphics.Bitmap
import com.example.crayonmarket.firebase.dto.UserDto
import com.example.crayonmarket.model.UserDetail
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object UserRepository {

    const val PAGE_SIZE = 30

    private var allUserList: MutableList<UserDto>? = null

    suspend fun getAllUserList(): Result<List<UserDto>> {
        if (allUserList != null) {
            return Result.success(requireNotNull(allUserList))
        }
        val userCollection =
            Firebase.firestore.collection("users").orderBy("name").limit(PAGE_SIZE.toLong())
        try {
            val userSnapshot = userCollection.get().await()
            if (userSnapshot.isEmpty) {
                allUserList = mutableListOf()
                return Result.success(requireNotNull(allUserList))
            }
            allUserList = userSnapshot.documents.map {
                requireNotNull(it.toObject(UserDto::class.java))
            }.toMutableList()
            return Result.success(requireNotNull(allUserList))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun saveInitUserInfo(
        name: String, profileImage: Bitmap?
    ): Result<Unit> {
        val user = Firebase.auth.currentUser
        require(user != null)
        val userDto = UserDto(
            uuid = user.uid, name = name, email = user.email
        )

        val userReference = Firebase.firestore.collection("users").document(user.uid)

        try {
            userReference.set(userDto).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (profileImage == null) {
            return Result.success(Unit)
        }

        val uuid = UUID.randomUUID().toString()
        val imageUrl = "${uuid}.png"
        val imageReference = Firebase.storage.reference.child(imageUrl)
        val byteArrayOutputStream = ByteArrayOutputStream()

        profileImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        try {
            imageReference.putBytes(data).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val newUserDto = userDto.copy(profileImageUrl = imageUrl)

        try {
            userReference.set(newUserDto).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    suspend fun updateInfo(
        name: String,
        profileImage: Bitmap?,
        isChangedImage: Boolean,
    ): Result<Unit> {

        val user = Firebase.auth.currentUser
        require(user != null)

        val userMap = mutableMapOf<String, Any>(
            "name" to name
        )

        val userReference = Firebase.firestore.collection("users").document(user.uid)

        if (isChangedImage && profileImage == null) {
            userMap["profileImageUrl"] = FieldValue.delete()
        } else if (isChangedImage && profileImage != null) {
            val uuid = UUID.randomUUID().toString()
            val imageUrl = "${uuid}.png"
            val imageReference = Firebase.storage.reference.child(imageUrl)
            val byteArrayOutputStream = ByteArrayOutputStream()

            profileImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val data = byteArrayOutputStream.toByteArray()

            try {
                imageReference.putBytes(data).await()
            } catch (e: Exception) {
                return Result.failure(e)
            }

            userMap["profileImageUrl"] = imageUrl
        }

        try {
            userReference.update(userMap.toMap()).await()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    suspend fun getUserDetail(userUuid: String): Result<UserDetail> {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val userCollection = db.collection("users")
        check(currentUser != null)

        return try {
            val userDto =
                userCollection.document(userUuid).get().await().toObject(UserDto::class.java)!!
            Result.success(
                UserDetail(
                    uuid = userDto.uuid,
                    name = userDto.name,
                    email = userDto.email,
                    profileImageUrl = userDto.profileImageUrl
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

}
