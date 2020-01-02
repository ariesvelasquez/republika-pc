package ariesvelasquez.com.republikapc

object Const {

    const val BASE_URL = BUILD_CONFIG_BASE_URL

    const val USERS_COLLECTION = "users"
    const val RIGS_COLLECTION = "rigs"
    const val SAVED_COLLECTION = "saved"
    const val RIGS_ITEM_COLLECTION = "rig_items"
    const val ITEM_PER_PAGE_20 : Long = 20
    const val ITEM_PER_PAGE_10 : Long = 10
    const val ITEM_PER_PAGE_5 : Long = 5
    const val RC_SIGN_IN = 123

    // Rigs Fields
    const val OWNER_ID = "ownerId"

    const val FIREBASE_ERROR_UNAUTHORIZED = "PERMISSION_DENIED: Missing or insufficient permissions."

    const val TIPID_PC_RAW_URL = "tipidpc.com"
    const val TIPID_PC_BASE_URL = "https://tipidpc.com/"
    const val TIPID_PC_VIEW_ITEM = TIPID_PC_BASE_URL + "viewitem.php?iid="
}

