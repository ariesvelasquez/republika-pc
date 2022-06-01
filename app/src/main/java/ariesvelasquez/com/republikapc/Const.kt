package ariesvelasquez.com.republikapc

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

object Const {

    const val BASE_URL = BUILD_CONFIG_BASE_URL

    const val USERS_COLLECTION = BUILD_CONFIG_USERS_COLLECTION
    const val RIGS_COLLECTION = BUILD_CONFIG_RIGS_COLLECTION
    const val SAVED_COLLECTION = BUILD_CONFIG_SAVED_COLLECTION
    const val RIGS_ITEM_COLLECTION = BUILD_CONFIG_RIGS_ITEM_COLLECTION
    const val FOLLOWED_COLLECTION = BUILD_CONFIG_FOLLOWED_TPC_SELLER
    const val ITEM_PER_PAGE_20 : Long = 20
    const val ITEM_PER_PAGE_10 : Long = 10
    const val ITEM_PER_PAGE_5 : Long = 5
    const val RC_SIGN_IN = 123

    // Firestore Fields
    const val OWNER_ID = "ownerId"
    const val DOC_ID = "docId"
    const val NAME = "name"
    const val SELLER = "seller"
    const val PRICE = "price"
    const val POST_DATE = "postDate"
    const val LINK_ID = "linkId"
    const val FIRST_LETTER = "firstLetter"
    const val CREATED_DATE = "createdDate"
    const val TPC_SELLER_USERNAME = "tpcSellerUsername"
    const val TYPE = "type"

    const val FIREBASE_ERROR_UNAUTHORIZED = "PERMISSION_DENIED: Missing or insufficient permissions."
    const val NO_CONNECTION_ERROR = "Unable to resolve host \"republika-pc-api.herokuapp.com\": No address associated with hostname"
    const val FAILED_TO_CONNECT_ERROR = "Failed to connect to"

    const val TIPID_PC_RAW_URL = "tipidpc.com"
    const val TIPID_PC_BASE_URL = "https://tipidpc.com/"
    const val TIPID_PC_VIEW_ITEM = TIPID_PC_BASE_URL + "viewitem.php?iid="
    const val TIPID_PC_VIEW_SELLER = TIPID_PC_BASE_URL + "useritems.php?username="

    object SaveType {
        const val TPC_ITEM = "tpcItem"
    }

    object Drawables {

        fun getFollowUnfollowIcon(context: Context, isFollowed: Boolean) : Drawable? {
            return when (isFollowed) {
                true -> ContextCompat.getDrawable(context, R.drawable.ic_vector_person_check)
                else -> ContextCompat.getDrawable(context, R.drawable.ic_vector_person)
            }
        }
    }
}

