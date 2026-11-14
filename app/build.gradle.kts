import java.net.URI

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services) apply false
}

// Explicit opt-in prevents an absent/private config from breaking Demo Mode builds.
val firebaseConfigured = providers.gradleProperty("campusq.firebase.enabled")
    .map(String::toBooleanStrict).getOrElse(false)
if (firebaseConfigured) {
    check(file("google-services.json").isFile) {
        "Firebase enabled: provide genuine app/google-services.json for com.example.campusq."
    }
    apply(plugin = "com.google.gms.google-services")
}

val localBackendUrl = providers.gradleProperty("campusq.backend.url").getOrElse("http://10.0.2.2:8787").trimEnd('/')
val localBackendUri = URI(localBackendUrl)
check(localBackendUri.scheme in setOf("http", "https") && localBackendUri.host?.matches(Regex("[A-Za-z0-9.:-]+")) == true &&
    localBackendUri.userInfo == null && localBackendUri.query == null && localBackendUri.fragment == null &&
    localBackendUri.path.orEmpty().isEmpty()) { "campusq.backend.url must be an HTTP(S) origin without credentials or path." }
abstract class GenerateCampusQNetworkConfig : DefaultTask() {
    @get:Input abstract val backendHost: Property<String>
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty
    @TaskAction fun generate() {
        val xml = outputDirectory.get().file("xml/campusq_local_network_security_config.xml").asFile
        xml.parentFile.mkdirs()
        val hosts = setOf(backendHost.get(), "10.0.2.2", "127.0.0.1", "localhost")
        xml.writeText("<network-security-config><base-config cleartextTrafficPermitted=\"false\"/><domain-config cleartextTrafficPermitted=\"true\">" +
            hosts.joinToString("") { "<domain includeSubdomains=\"false\">$it</domain>" } + "</domain-config></network-security-config>")
    }
}
val generateLocalNetworkConfig = tasks.register<GenerateCampusQNetworkConfig>("generateLocalNetworkConfig") {
    backendHost.set(localBackendUri.host)
    outputDirectory.set(layout.buildDirectory.dir("generated/localNetwork/res"))
}
androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        variant.sources.res?.addGeneratedSourceDirectory(generateLocalNetworkConfig, GenerateCampusQNetworkConfig::outputDirectory)
    }
}

android {
    namespace = "com.example.campusq"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.campusq"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "LOCAL_BACKEND_URL", "\"" + localBackendUrl + "\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Keep automatic discovery scoped to app tests as the SDK classpath grows.
        testInstrumentationRunnerArguments["package"] = "com.example.campusq"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.messaging)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}