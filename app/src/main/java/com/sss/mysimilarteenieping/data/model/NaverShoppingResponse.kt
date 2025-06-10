package com.sss.mysimilarteenieping.data.model

import com.google.gson.annotations.SerializedName

data class NaverShoppingResponse(
    @SerializedName("lastBuildDate")
    val lastBuildDate: String,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("start")
    val start: Int,
    
    @SerializedName("display")
    val display: Int,
    
    @SerializedName("items")
    val items: List<NaverShoppingItem>
)

data class NaverShoppingItem(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("link")
    val link: String,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("lprice")
    val lowPrice: String,
    
    @SerializedName("hprice")
    val highPrice: String,
    
    @SerializedName("mallName")
    val mallName: String,
    
    @SerializedName("productId")
    val productId: String,
    
    @SerializedName("productType")
    val productType: String,
    
    @SerializedName("brand")
    val brand: String,
    
    @SerializedName("maker")
    val maker: String,
    
    @SerializedName("category1")
    val category1: String,
    
    @SerializedName("category2")
    val category2: String,
    
    @SerializedName("category3")
    val category3: String,
    
    @SerializedName("category4")
    val category4: String
) 