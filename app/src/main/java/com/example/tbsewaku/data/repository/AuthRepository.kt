package com.example.tbsewaku.data.repository

import com.example.tbsewaku.data.api.ApiService
import com.example.tbsewaku.data.preferences.SharedPrefsManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AuthRepository(
    private val apiService: ApiService,
    private val sharedPrefsManager: SharedPrefsManager
) {

    // Fungsi login tanpa model
    suspend fun login(username: String, password: String): Boolean {
        val response = apiService.login(mapOf("username" to username, "password" to password))

        if (response.isSuccessful) {
            val responseBody = response.body()
             println("DEBUG: Raw Response: ${response.raw()}")
            println("DEBUG: Response Code: ${response.code()}")
            println("DEBUG: Response Body: ${response.body()}")
            val message = responseBody?.get("message") as? String
                val data = responseBody?.get("data") as? Map<String, Any>
                val token = data?.get("token") as? String
                val user = data?.get("user") as? Map<String, Any>
                   println("DEBUG: Response usery: ${user}")
                   println("DEBUG: Response token: ${token}")
                if (token != null && user != null) {
                    // Simpan token dan data user
                    val userId = (user["id"] as Double).toInt()
                    val username = user["username"] as? String
                    println("DEBUG: Response username: ${username}")
                    println("DEBUG: Response userid: ${userId}")
                    if (userId != null && username != null) {
                        sharedPrefsManager.saveLoginData(token, userId, username)
                        return true
                    }
                }
        }
        return false
    }

    suspend fun register(email: String, username: String, password: String): Boolean {
        return try {
            val response = apiService.register(mapOf(
                "email" to email,
                "username" to username,
                "password" to password
            ))
            
            println("DEBUG: Register Raw Response: ${response.raw()}")
            println("DEBUG: Register Response Code: ${response.code()}")
            println("DEBUG: Register Response Body: ${response.body()}")
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                val data = responseBody["data"] as Map<*, *>
                val user = data["user"] as Map<*, *>
                val token = data["token"] as String
                val userId = (user["id"] as Double).toInt()
                val username = user["username"] as String
                
                println("DEBUG: Register Token: $token")
                println("DEBUG: Register UserId: $userId")
                println("DEBUG: Register Username: $username")
                
                sharedPrefsManager.saveLoginData(token, userId, username)
                true
            } else {
                println("DEBUG: Register failed")
                false
            }
        } catch (e: Exception) {
            println("DEBUG: Register exception - ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserSelf(token: String): Map<String, Any>? {
    try {
        val response = apiService.getUserSelf("Bearer $token")
        if (response.isSuccessful) {
            val responseBody = response.body()
            val data = responseBody?.get("data") as? Map<String, Any>
            return data
        }
    } catch (e: Exception) {
        println("DEBUG: GetUserSelf exception - ${e.message}")
    }
    return null
}

suspend fun updateUser(
    token: String,
    username: String,
    email: String,
    address: String?,
    latitude: String?,
    longitude: String?
): Boolean {
    try {
        val requestBody = mapOf(
            "username" to username,
            "email" to email,
            "address" to (address ?: ""),
            "latitude" to (latitude ?: ""),
            "longitude" to (longitude ?: "")
        )

        val response = apiService.updateUser(
            body = requestBody,
            token = "Bearer $token"
        )
        return response.isSuccessful
    } catch (e: Exception) {
        println("DEBUG: UpdateUser exception - ${e.message}")
        return false
    }
}

suspend fun createProduct(
    token: String,
    name: String,
    description: String,
    type: String,
    price: String,
    stock: String,
    imageFile: File
): Boolean {
    return try {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)
        
        val namePart = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val descriptionPart = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        val typePart = RequestBody.create("text/plain".toMediaTypeOrNull(), type.toString())
        val pricePart = RequestBody.create("text/plain".toMediaTypeOrNull(), price)
        val stockPart = RequestBody.create("text/plain".toMediaTypeOrNull(), stock)

        val response = apiService.createProduct(
            name = namePart,
            description = descriptionPart,
            type = typePart,
            price = pricePart,
            stock = stockPart,
            image = imagePart,
            token = "Bearer $token"
        )

        response.isSuccessful
    } catch (e: Exception) {
        println("DEBUG: CreateProduct exception - ${e.message}")
        false
    }
}


suspend fun getOrders(token: String): List<Map<String, Any>>? {
    try {
        val response = apiService.getOrders("Bearer $token")
        if (response.isSuccessful) {
            val responseBody = response.body()
            val data = responseBody?.get("data") as? List<Map<String, Any>>
            println("DEBUG: Orders Response: $data")
            return data
        }
    } catch (e: Exception) {
        println("DEBUG: GetOrders exception - ${e.message}")
    }
    return null
}

}