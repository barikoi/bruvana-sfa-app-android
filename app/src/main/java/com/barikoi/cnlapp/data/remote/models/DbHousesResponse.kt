package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class DbHousesResponse(
    @SerializedName("db_houses")
    val dbHouses: List<DbHouse>,
    @SerializedName("status_code")
    val statusCode: Int
)

data class DbHouse(
    @SerializedName("area_code")
    val areaCode: String,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("area_name")
    val areaName: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("db_house_address")
    val dbHouseAddress: String,
    @SerializedName("db_house_code")
    val dbHouseCode: String,
    @SerializedName("db_house_name")
    val dbHouseName: String,
    @SerializedName("db_house_owner_name")
    val dbHouseOwnerName: String,
    @SerializedName("db_house_owner_phone_number")
    val dbHouseOwnerPhoneNumber: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("region_code")
    val regionCode: String,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("region_name")
    val regionName: String,
    @SerializedName("territory_code")
    val territoryCode: String,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("territory_name")
    val territoryName: String,
    @SerializedName("updated_at")
    val updatedAt: String
)