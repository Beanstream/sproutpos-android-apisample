<img src="http://www.beanstream.com/wp-content/uploads/2015/08/Beanstream-logo.png" />
# Beanstream Android SDK Sample App
<img align="right" src="screenshot.png" width=200px />
The purpose of this sample app is to show how to use the Beanstream SDK for Android.
 
The sample app allows you to sell Golden Eggs for $5.00, as well as search for transactions and see receipts.  It includes at least one call to every API available in our SDK.
 
Our SDK uses gradle to automate it's dependencies. The sample app includes the SDK and an optional API simulator which is also available through gradle (https://github.com/Beanstream/sproutpos-android-apisimulator).  

Note that the Beanstream SDK itself has it's own gradle dependencies that include: OkHttp, Retrofit, EventBus, Gson, Jodatime which you can see in the build.gradle file.

To use the SDK and Simulator you will require artifactory credentials. You can sign up to receive credentials from [here](http://developer.beanstream.com/documentation/sprout/)

To be able to compile this project you can clone the git source repo to a working directory.  

## 2.) Setup Beanstream SDK Sample App

```
> git clone https://github.com/Beanstream/sproutpos-android-apisample
> Open in Android Studio
> Open the gradle.properties file and add your artifactory credentials so gradle can sync the libraries.
> Run build->clean project
```

For more info on how to use the Beanstream SDK check out [developer.beanstream.com](http://developer.beanstream.com/documentation/sprout-sdk-android/).

## 3.) Simulator vs Live

The sample app starts in simulation mode.  You don't need any Beanstream credentials to log into an account in this mode, it will accept any credentials.  To change between simulator and live you can do so in the GoldenEggsApplication.java file

````
>  public boolean isSimulation() {
>        return true;
>  }
````
