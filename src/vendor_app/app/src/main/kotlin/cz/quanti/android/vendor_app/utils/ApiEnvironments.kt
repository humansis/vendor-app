package cz.quanti.android.vendor_app.utils

import cz.quanti.android.vendor_app.BuildConfig

enum class ApiEnvironments(val id: Int, val secure: Boolean, val port: Int?) {
    FRONT(0, true, null) {
        override fun getUrl() = BuildConfig.FRONT_API_URL
    },
    DEMO(1, true, null) {
        override fun getUrl() = BuildConfig.DEMO_API_URL
    },
    STAGE(2, true, null) {
        override fun getUrl() = BuildConfig.STAGE_API_URL
    },
    DEV1(3, true, null) {
        override fun getUrl() = BuildConfig.DEV1_API_URL
    },
    DEV2(4, true, null) {
        override fun getUrl() = BuildConfig.DEV2_API_URL
    },
    DEV3(5, true, null) {
        override fun getUrl() = BuildConfig.DEV3_API_URL
    },
    TEST(6, true, null) {
        override fun getUrl() = BuildConfig.TEST_API_URL
    },
    LOCAL(7, false, 8091) {
        override fun getUrl() = BuildConfig.LOCAL_API_URL
    };

    abstract fun getUrl(): String
}
