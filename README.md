# Facial Recognition with Weka ML (Free!)

**The purpose of this Android app is to use the Weka Machine Learning Library for Java in Android Studio to implement facial recognition FOR FREE by training 
classification algorithms and a neural network on users' facial data. The facial data is collected through the Microsoft Face API and Firebase.**

Features of this app include:
 - **Registering users** given their image and name.
 - **Identifying users** when given their image.
 - **Displaying facial data** being stored on device.

###### Please note that the app may not be 100% accurate for very large numbers of users. Basic logic is in place to determine the closest match from what the algorithms have classified and their confidences. For improved accuracy, go ahead and make your own tweaks to the function called ["bestMatch()" on Line 421](https://github.com/ishaanjav/Weka-ML-Face-Recognition/blob/master/app/src/main/java/com/example/anany/javawekamlfacerecognition/MainActivity.java#L421).
_____
## Table of Contents
  - ### [Usage](https://github.com/ishaanjav/Weka-ML-Face-Recognition/blob/master/README.md#usage-1)
  - ### [How it Works](#How-it-Works)
  - ### [Setup](#Setup) (Including getting the Microsoft Face API For Free)
  - ### [Customizability and Improving the App](#Customizability-and-Improving-the-App)
  - ### [Privacy / Security Concerns](https://github.com/ishaanjav/Weka-ML-Face-Recognition/blob/master/README.md#privacy--security-concerns-1)
  - ### [Other Repositories](#Other-Repositories)

### The [Wiki]() contains information on how Machine Learning classification algorithms and neural networks work. If you do not have prior knowledge or experience with ML, it is highly recommended that you check it out to [learn about machine learning]().
_____
<img align="right" src="https://github.com/ishaanjav/Face_Analyzer/blob/master/Face%20Analyzer%20Demo.gif" width="250" />

## Usage:

**The app is simple enough to use:** First, you must get data for the classification algorithms to train on by:
   - **Enter a name** in the EditText
   - Repeatedly use **"Register A Face"** to get the facial data of the person. *(This should be done at least 6 or 7 times for best accuracy.)*
   - Once you have a sufficient # of people and facial data for them, click **"Recognize A Face"** to recognize the face. An `AlertDialog` should pop up with the match.

**Extra Info:**
   - Click the **"Read Data"** button for an `AlertDialog` displaying the facial data from internal storage.
   - After recognizing a face, press **"Extra Stats"** to see statistics about the algorithms' and neural network's performance.

**Now to use the app, head on over to [Setup](#Setup)**
_____

## How it Works:
The app uses **Firebase** to get facial data such as the distance between the cheeks, eyes, etc. Then it uses the **Microsoft Face API** in an `AsyncTask` to get the head rotation and estimated age. This is info is then saved in local storage.

Then, when the recognize button is pressed, it gets the same data except now trains the classification algorithms and neural network on the data to classify the new instance.
Once that's done, the function I wrote, [**"bestMatch()"**](https://github.com/ishaanjav/Weka-ML-Face-Recognition/blob/master/app/src/main/java/com/example/anany/javawekamlfacerecognition/MainActivity.java#L421)
, which uses basic logic of the classifications and confidences to get the closest match that is then displayed.

_____
## Setup:

***You do not need to worry about installing the Weka ML Library because the files are already in this repo***.

*Please note that this app requires the use of [**Microsoft Azure's Face API**](https://azure.microsoft.com/en-us/services/cognitive-services/face/).* **Without an API Key, you will not be able to use the app as it was intended.** The following sections contain the full set of instructions to getting your own API key for free and using it in the app by changing a single line of code.
### Downloading to Android Studio
To use the app, you can clone it from this GitHub repository as a zip file, extract the contents of the file, and open it as a project in Android Studio. Once you have done so, it can be run on your Android device.
### Making the Azure Account
In order to run the face dectection and analysis, you must get an API Subscription Key from the Azure Portal. [This page](https://azure.microsoft.com/en-us/services/cognitive-services/face/) by Microsoft provides the features and capabilities of the Face API. 

**You can create a free Azure account that doesn't expire at [this link here](https://azure.microsoft.com/en-us/try/cognitive-services/?api=face-api) by clicking on the "Get API Key" button and choosing the option to create an Azure account**. 
### Getting the Face API Key from Azure Portal
Once you have created your account, head to the [Azure Portal](https://portal.azure.com/#home). Follow these steps:
1. Click on **"Create a resource"** on the left side of the portal.
2. Underneath **"Azure Marketplace"**, click on the **"AI + Machine Learning"** section. 
3. Now, under **"Featured"** you should see **"Face"**. Click on that.
4. You should now be at [this page](https://portal.azure.com/#create/Microsoft.CognitiveServicesFace). **Fill in the required information and press "Create" when done**.
5. Now, click on **"All resources"** on the left hand side of the Portal.
6. Click on the **name you gave the API**.
7. Underneath **"Resource Management"**, click on **"Manage Keys"**.

<p align="center">
  <img width="900" src="https://github.com/ishaanjav/Face_Analyzer/blob/master/Azure-FaceAPI%20Key.PNG">
 
</p>

You should now be able to see two different subscription keys that you can use. Follow the additional instructions to see how to use the API Key in the app.
### Using the API Key in the app
Head over to the [MainActivity page](https://github.com/ishaanjav/Weka-ML-Face-Recognition/blob/master/app/src/main/java/com/example/anany/javawekamlfacerecognition/MainActivity.java#L156) in Android Studio since that is where the API Key will be used when creating the `FaceServiceClient` object. Where it says in `onCreate`:

    faceServiceClient = new FaceServiceRestClient("<YOUR ENDPOINT HERE>", "<YOUR API KEY>"); 

replace `<YOUR API KEY>` with one of your 2 keys from the Azure Portal. *(If you haven't gotten your API Key yet, read the previous two sections above)*. `<YOUR ENDPOINT HERE>` should be replaced with one of the following examples from [this API Documentation link](https://westus.dev.cognitive.microsoft.com/docs/services/563879b61984550e40cbbe8d/operations/563879b61984550f30395236). The format should be similar to: 
  
    "https://<LOCATION>/face/v1.0"
  
where `<LOCATION>` should be replaced with something like `uksouth.api.cognitive.microsoft.com` or `japaneast.api.cognitive.microsoft.com`. All of these can be found, listed at [this link](https://westus.dev.cognitive.microsoft.com/docs/services/563879b61984550e40cbbe8d/operations/563879b61984550f30395236).

### Setup is complete!
*Please note that if you are using the free, standard plan, you can only make 20 API transactions/calls per minute. Therefore, if that limit is exceeded, you may run into runtime errors.*
_____
## Customizability and Improving the App

### bestMatch() Function: 
As mentioned earlier, basic logic is used to find the best match of a user's face using the algorithms' classifications and their confidences. You can go ahead and improve the logic by changing the function [**"bestMatch()" on Line 421**](https://github.com/ishaanjav/Weka-ML-Face-Recognition/blob/master/app/src/main/java/com/example/anany/javawekamlfacerecognition/MainActivity.java#L421) by using extra conditional statements. 
I have found the following general patterns *(which may not always apply)*:
   - Decision Tree Confidence is almost always 0% or 100%.
   - The KNN Algorithm and Neural Network are most often correct.
   - When both the KNN and Neural Network have high confidences, their classifications are most likely accurate.

**You can use these observations to add if statements to check how confident the KNN and NN are and then prioritize their classifications, and so on.**

### Additional Facial Features:
The accuracy can be improved by adding better, more unique facial features. **One excellent example of this is [gender which you can get with the Microsoft Face API](https://github.com/ishaanjav/Face_Analyzer/blob/master/README.md#detecting-particular-facial-attributes)**. 

Because gender is binary, it will make it even easier for the algorithms to recognize a face because if an instance of data is of the opposite gender of what is being recognized, then obviously that cannot be a possible candidate.

**Attributes Worth Exploring:**
   - Gender
   - Hair Color
   - Size of face

**To learn how to get these attributes from the Face API, check out [this link](https://github.com/ishaanjav/Face_Analyzer/blob/master/README.md#detecting-particular-facial-attributes) where I have explained how to detect particular facial attributes.**

_____
## Privacy / Security Concerns
**Regarding Privacy Concerns:** The app was designed so that the **facial data is stored on the device** and not in Firebase or anywhere else. Therefore, **only the user of the device can see the facial data**.

Even so, the facial data is **not at all uniquely identifiable** and the **images are not stored anywhere**.

_____
## Other Repositories
If you liked this, I have some other great repositories that make use of APIs like the [Microsoft Face API](https://azure.microsoft.com/en-us/services/cognitive-services/face/), [Kairos API](https://www.kairos.com/docs/), [Google Maps SDK](https://developers.google.com/maps/documentation/android-sdk/intro) and more. 

**My most popular repository is the [Kairos Face Recognition](https://github.com/ishaanjav/Kairos_Face_Recognition) which I highly recommend. *(It is the easiest method of implementing facial recognition by far)***. Below is a list of some of my repositories:

- [**Kairos Face Recognition**](https://github.com/ishaanjav/Kairos_Face_Recognition): The purpose of this Android app is to use Kairos's SDK for Android in order to implement facial recognition. Features of this app include: registering and identifying users when given an image. [README](https://github.com/ishaanjav/Kairos_Face_Recognition/blob/master/README.md)

- [**Face Analyzer**](https://github.com/ishaanjav/Face_Analyzer): The purpose of this Android app is to utilize the Microsoft Face API to not only detect individual faces in an image, but also for each face provide information such as emotions, the estimated age, gender, and more. [README](https://github.com/ishaanjav/Face_Analyzer/blob/master/README.md)  [**App on Google PlayStore**](https://play.google.com/store/apps/details?id=app.anany.faceanalyzer)


