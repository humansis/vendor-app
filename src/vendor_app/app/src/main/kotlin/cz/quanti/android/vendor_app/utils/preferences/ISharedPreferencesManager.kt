package cz.quanti.android.vendor_app.utils.preferences

interface ISharedPreferencesManager {

    /*
    * Save value into shared preferences according to type of input value
    * */
    fun saveValue(key: String, value: Any)

    /*
    * Get value from shared preferences according to default value type
    * */
    fun getValue(key: String, defaultValue: Any): Any

    /*
    * Clear all values stored in shared preferences
    * */
    fun clearAll()
}
