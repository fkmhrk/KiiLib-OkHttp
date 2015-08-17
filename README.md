KiiLib OkHttp
========

This is the library for Kii Cloud with OkHttp implementation. This library depends on the following libraries. 
* OkHttp
* KiiLib-Java

How to add this library on your project
==========
Step 1. Clone!

    git clone https://github.com/fkmhrk/KiiLib-OkHttp.git

Step 2. Copy m2repository folder to your project

    AppProject
      - app
        - build.gradle
      - m2repository
      - build.gradle

Step 3. Add the following entries to your app/build.gradle

    repositories {
        maven {
            url "../m2repository"
        }
    }

    dependencies {
        compile 'jp.fkmsoft.libs:KiiLib-OkHttp:1.0.0'
        compile fileTree(dir: 'libs', include: ['*.jar'])
        // add other dependencies here
    }
    
Step 4. Click "Sync Project with Gradle Files" on Android Studio.
