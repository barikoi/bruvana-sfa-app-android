package com.barikoi.cnlapp.data.remote.models


import com.google.gson.annotations.SerializedName

data class RequestStockResponse(
    @SerializedName("data")
    val data: List<RequestStock>,
    @SerializedName("message")
    val message: String,
    @SerializedName("status_code")
    val statusCode: Int
)

data class RequestStock(
    @SerializedName("area")
    val area: Any,
    @SerializedName("area_id")
    val areaId: Any,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("data")
    val stockProducts: List<StockProduct>,
    @SerializedName("db_house")
    val dbHouse: DbHouse?,
    @SerializedName("db_house_id")
    val dbHouseId: Int?,
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("request_from_user")
    val requestFromUser: RequestFromUserX,
    @SerializedName("request_to_user")
    val requestToUser: RequestToUser,
    @SerializedName("requested_to")
    val requestedTo: Int,
    @SerializedName("responded_at")
    val respondedAt: String,
    @SerializedName("responded_by")
    val respondedBy: Int,
    @SerializedName("remarks")
    val remarks: String?,
    @SerializedName("responded_by_user")
    val respondedByUser: RespondedByUser?,
    @SerializedName("route")
    val route: Any,
    @SerializedName("route_id")
    val routeId: Any,
    @SerializedName("status")
    val status: String,
    @SerializedName("territory")
    val territory: Territory,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_id")
    val userId: Int
)

data class StockProduct(
    @SerializedName("area_id")
    val areaId: Any,
    @SerializedName("current_available_stock")
    val currentAvailableStock: Int,
    @SerializedName("db_house_id")
    val dbHouseId: Int,
    @SerializedName("id")
    val id: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_name")
    val productName: String?,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("status")
    val status: Int,
    @SerializedName("approve_stock")
    val approveStock: Int
)


data class RespondedByUser(
    @SerializedName("app_version")
    val appVersion: Any,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("db_house_id")
    val dbHouseId: Int,
    @SerializedName("designation")
    val designation: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: Any,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("phone")
    val phone: Any,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("supervisor_id")
    val supervisorId: Any,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_name")
    val userName: String
)


data class RequestFromUserX(
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("db_house_id")
    val dbHouseId: Int,
    @SerializedName("designation")
    val designation: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: Any,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("supervisor_id")
    val supervisorId: Any,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_name")
    val userName: String
)

data class RequestToUser(
    @SerializedName("app_version")
    val appVersion: String,
    @SerializedName("area_id")
    val areaId: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("db_house_id")
    val dbHouseId: Int,
    @SerializedName("designation")
    val designation: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any,
    @SerializedName("employee_id")
    val employeeId: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("images")
    val images: Any,
    @SerializedName("is_active")
    val isActive: Int,
    @SerializedName("nation_id")
    val nationId: Int,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("region_id")
    val regionId: Int,
    @SerializedName("supervisor_id")
    val supervisorId: Any,
    @SerializedName("territory_id")
    val territoryId: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user_name")
    val userName: String
)

data class Territory(
    @SerializedName("id")
    val id: Int,
    @SerializedName("territory_name")
    val territoryName: String
)
//"id": 3,
//"status": 3,
//"db_house_id": "2",
//"product_name": "Comfort L",
//"approve_stock": 3,
//"current_available_stock": 5