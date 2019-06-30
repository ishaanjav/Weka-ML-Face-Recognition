package com.example.anany.javawekamlfacerecognition;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.gson.Gson;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.Hair;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.NormalizedPolyKernel;
import weka.classifiers.functions.supportVector.PolyKernel;
import weka.classifiers.functions.supportVector.Puk;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.mi.supportVector.MIPolyKernel;
import weka.classifiers.mi.supportVector.MIRBFKernel;
import weka.classifiers.trees.J48;
import weka.core.Debug;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class MainActivity extends AppCompatActivity {

    TextView display;
    Button read, register, identify;
    ImageView image;
    EditText name;
    int clicks = 0;

    String age;

    ArrayList<String> names = new ArrayList<>();
    private FaceServiceClient faceServiceClient;

    //Read facial data from device storage0-
    public BufferedReader readDataFile(String filename) {
        BufferedReader br = null;
        BufferedReader inputReader = null;
        try {
            FileInputStream fis = getApplication().openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);

            String yourFilePath = getApplicationContext().getFilesDir() + "/" + filename;
            File yourFile = new File(yourFilePath);
            inputReader = new BufferedReader(new FileReader(yourFile));

            br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            String lines = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
                lines += line + "\n";
            }
            //display.setText(lines);
        } catch (Exception e) {
            longToast("Message: " + e.getMessage() + " Cause: " + e.getCause());
            display.setText("Message: " + e.getMessage() + " Cause: " + e.getCause());
        }
        return inputReader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = findViewById(R.id.textView);
        read = findViewById(R.id.read);
        register = findViewById(R.id.register);
        identify = findViewById(R.id.recognize);
        image = findViewById(R.id.imageView);
        name = findViewById(R.id.name);
        faceServiceClient = new FaceServiceRestClient("<YOUR API ENDPOINT>", "<YOUR API KEY>");


        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1000);
        }


        image.setVisibility(View.VISIBLE);

        //Set On Click Listener for the "Read Data Button"
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //readData();
                if (clicks == 0) {
                    BufferedReader bufferedReader = readDataFile("faceData");
                    image.setVisibility(View.INVISIBLE);
                    if (bufferedReader == null) {
                        makeToast("No data in specified file");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        String lines = "";
                        try {
                            while ((line = bufferedReader.readLine()) != null) {
                                sb.append(line);
                                lines += line + "\n";
                            }
                        } catch (Exception e) {
                            makeToast(e.getMessage());
                        }

                        readInfo(lines);
                        //display.setText(lines);
                    }
                    read.setText("Show Image!");
                    clicks = 1;
                } else if (clicks == 1) {
                    display.setText("");
                    read.setText("Read Data!");
                    image.setVisibility(View.VISIBLE);

                    clicks = 0;
                }
            }
        });

        //Detect when Register button is pressed
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  writeToFile();
                if (name.getText().toString().isEmpty()) {
                    makeToast("Please enter a name.");
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 1);
                }
            }
        });

        //Detect when Recognize button is pressed.
        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  runML();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 2);
            }
        });


    }

    //Display the info from storage in and Alert Dialog.
    public void readInfo(String info) {
        final Dialog alert = new Dialog(MainActivity.this);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.view_data);
        alert.setCancelable(true);

        TextView text = alert.findViewById(R.id.text);
        text.setText(info);

        Button exit = alert.findViewById(R.id.done);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                alert.cancel();
            }
        });

        alert.show();
    }

    //Given the HashMap with data, make a prediction.
    private Object makePrediction(HashMap<String, String> hashMap) {
        float cheek = Float.parseFloat(hashMap.get("Cheek diff"));
        float eye = Float.parseFloat(hashMap.get("Eye diff"));
        float mouth = Float.parseFloat(hashMap.get("Mouth diff"));
        float nose = Float.parseFloat(hashMap.get("Nose diff"));
        double age = Double.parseDouble(hashMap.get("Age"));

        String yourFilePath = getApplicationContext().getFilesDir() + "/" + "faceData";
        File yourFile = new File(yourFilePath);
        try {
            BufferedReader all = new BufferedReader(new FileReader(yourFile));
            Instances combined = new Instances(all);
            if (combined == null) {
                longToast("Combined is NULL. LINE 264.");
            }
            combined.setClassIndex(5);

            String yourPath = getApplicationContext().getFilesDir() + "/" + "names";
            File file = new File(yourPath);
            BufferedReader namesReader = new BufferedReader(new FileReader(file));

            String line1 = "";
            String line2 = "";
            String tline;
            int mycount = 0;
            while ((tline = namesReader.readLine()) != null) {
                if (mycount == 0) {
                    line1 += tline;
                    mycount++;
                } else {
                    line2 += tline;
                }
            }
            String[] numarray = line2.split(" ");
            String[] names = line1.split(" ");
            String name = numarray[0];
            for (int i = 1; i < numarray.length; i++) {
                name += ", " + numarray[i];
            }
            combined.randomize(new java.util.Random(0));

            SMO smo = new SMO();
            smo.buildClassifier(combined);

            J48 j48 = new J48();
            j48.buildClassifier(combined);

            MultilayerPerceptron m = new MultilayerPerceptron();
            m.setLearningRate(0.1);
            m.setMomentum(0.2);
            m.setTrainingTime(4000);
            m.setHiddenLayers("11");
            m.buildClassifier(combined);

            Classifier ibk = new IBk(3);
            ibk.buildClassifier(combined);

            String relation = "@relation testdata";
            String attributes = "\n@attribute cheek numeric\n" +
                    "@attribute eye numeric\n" +
                    "@attribute mouth numeric\n" +
                    "@attribute nose numeric\n" +
                    "@attribute age numeric\n" +
                    "@attribute class {" + name + "}";

            String lines = "\n" + cheek + ", " + eye + ", " + mouth + ", " + nose + ", " + age + ", 1";
            String datat = "\n\n@data" + lines + "\n";
            //  longToast("This is the data being put in testData: " + lines);
            FileOutputStream stream;
            //longToast("Attributes: " + attributes);
            try {
                stream = openFileOutput("TestData", Context.MODE_PRIVATE);
                stream.write((relation + attributes + datat).getBytes());
                stream.close();
                makeToast("Made the file with new data!");
            } catch (Exception ex) {
                longToast("Message: " + ex.getMessage() + " Cause: " + ex.getCause());
                display.setText("LINE 331 Message: " + ex.getMessage() + " Cause: " + ex.getCause());
            }

            String yourFilePath2 = getApplicationContext().getFilesDir() + "/" + "TestData";
            File yourFile2 = new File(yourFilePath2);
            BufferedReader inputReader2 = new BufferedReader(new FileReader(yourFile2));

            Instances test = new Instances(inputReader2);
            test.setClassIndex(5);
            Instance[] instances = new Instance[1];
            instances[0] = test.instance(0);

            double nnpredictions = m.classifyInstance(instances[0]);
            double predictions = ibk.classifyInstance(instances[0]);
            double svcprediction = smo.classifyInstance(instances[0]);
            double jprediction = j48.classifyInstance(instances[0]);

            int knum = (int) predictions;
            int snum = (int) svcprediction;
            int nnum = (int) nnpredictions;
            int jnum = (int) jprediction;
            String knnmatch = "";

            double[] nnconfidencea = m.distributionForInstance(test.instance(0));
            double[] dtconfidencea = j48.distributionForInstance(test.instance(0));
            double[] knnconfidencea = ibk.distributionForInstance(test.instance(0));
            double[] svcconfidencea = smo.distributionForInstance(test.instance(0));

            double nncofidence = nnconfidencea[nnum];
            double dtcofidence = dtconfidencea[jnum];
            double knncofidence = knnconfidencea[knum];
            double svccofidence = svcconfidencea[snum];

            Evaluation jeval = new Evaluation(combined);
            jeval.evaluateModel(j48, combined);
            //longToast("Decision Tree: " + jeval.toSummaryString());
            Evaluation seval = new Evaluation(combined);
            seval.evaluateModel(smo, combined);
            //longToast("SVC: " + seval.toSummaryString());
            Evaluation keval = new Evaluation(combined);
            keval.evaluateModel(ibk, combined);
            //longToast("KNN: " + keval.toSummaryString());
            Evaluation neval = new Evaluation(combined);
            neval.evaluateModel(m, combined);
            //longToast("NN: " + neval.toSummaryString());
            knnmatch = names[knum];
            String svcmatch = names[snum];
            String nnmatch = names[nnum];
            String info = "Decision Tree: " + jeval.toSummaryString() + "\nSVC EVALUATION: " + seval.toSummaryString() + "\nKNN EVALUATION: " + keval.toSummaryString() + "\nNN EVALUATION: " + neval.toSummaryString();

            String jmatch = names[jnum];
            TreeMap<String, Integer> treeMap = new TreeMap<>();
            ArrayList<String> strings = new ArrayList<>();
            strings.add(svcmatch);
            strings.add(nnmatch);
            strings.add(knnmatch);
            for (String w : strings) {
                Integer i = treeMap.get(w);
                if (i == null) {
                    treeMap.put(w, 1);
                } else {
                    treeMap.put(w, i + 1);
                }
            }

            //Get the best match from what the classifiers say first based on any ties and then their confidences.
            //It is a very basic function so please feel free to make changes to it as you wish.
            String bestMatch = bestMatch(nncofidence, dtcofidence, svccofidence, knncofidence, jmatch, knnmatch, svcmatch, nnmatch);


            //TODO Take confidences and use logic to determine best match. Then call showResults function.
            //TODO make toast of final name. Then call showResults function.
            makeToast("The person is most likely: " + bestMatch);
            showResults(bestMatch, info, nncofidence, dtcofidence, svccofidence, knncofidence, jmatch, knnmatch, svcmatch, nnmatch);
           /* makeToast(cheek + " " + eye + " " + mouth + " " + nose);
            makeToast("DT: " + jmatch + " " + jnum + "\nKNN: " + knnmatch + " " + knum + "\nSVC: " + svcmatch + " " + snum + "\nNeural Network: " + nnmatch + " " + nnum + "\nFalse Value: " + instances[0].classValue());
            longToast("DT: " + jmatch + " " + jnum + "\nKNN: " + knnmatch + " " + knum + "\nSVC: " + svcmatch + " " + snum + "\nNeural Network: " + nnmatch + " " + nnum + "\nFalse Value: " + instances[0].classValue());
            longToast("DT: " + jmatch + " " + jnum + "\nKNN: " + knnmatch + " " + knum + "\nSVC: " + svcmatch + " " + snum + "\nNeural Network: " + nnmatch + " " + nnum + "\nFalse Value: " + instances[0].classValue());
       */
        } catch (Exception ex) {
            longToast("LINE 416 Message: " + ex.getMessage() + " Cause: " + ex.getCause());
            display.setText("LINE 416 Message: " + ex.getMessage() + " Cause: " + ex.getCause() + "\n" + ex.toString());
        }
        return "hi";
    }

    //Given confidences for each ML algorithm as well as classifications, return a String with the best match.
    public String bestMatch(double nnCon, double dtCon, double svcCon, double knnCon, String jmatch, String knnmatch, String svcmatch, String nnmatch) {
        //TODO This is just a very basic method of getting the best possible match by checking if there were any ties between the
        //TODO 4 classification methods. If there is a tie, then confidence levels are used to break it.
        //TODO Otherwise, the matching name with the highest number of classifications is returned.

        //TODO Please feel free to make changes to this as you wish. I just wrote it in a hurry that way you had a way of
        //TODO seeing which match was the best.
        Set<String> allnames = new HashSet<>();
        allnames.add(jmatch);
        allnames.add(nnmatch);
        allnames.add(svcmatch);
        allnames.add(knnmatch);

        HashMap<String, Integer> occur = new HashMap<>();
        for (String s : allnames) {
            if (occur.containsKey(s)) {
                occur.put(s, occur.get(s) + 1);
            } else {
                occur.put(s, 1);
            }
        }

        Map.Entry<String, Integer> maxEntry = null;

        for (Map.Entry<String, Integer> entry : occur.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        int maxVal = maxEntry.getValue();
        ArrayList<String> possible = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : occur.entrySet()) {
            if (entry.getValue() == maxVal) {
                possible.add(entry.getKey());
            }
        }
        if (possible.size() == 1)
            return maxEntry.getKey();

        if (nnCon >= dtCon && nnCon >= knnCon && nnCon >= svcCon) {
            return nnmatch;
        } else if (knnCon >= dtCon && knnCon >= svcCon) {
            return knnmatch;
        } else if (dtCon >= svcCon) {
            return svcmatch;
        } else if (svcCon > dtCon && svcCon > knnCon && svcCon > nnCon) {
            return svcmatch;
        } else {
            return nnmatch;
        }
    }

    //Show the results in an Alert Dialog.
    public void showResults(String name, String info, double nnCon, double dtCon, double svcCon, double knnCon, String jmatch, String knnmatch, String svcmatch, String nnmatch) {
        final Dialog alert = new Dialog(MainActivity.this);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.results);
        alert.setCancelable(true);

        TextView usName = alert.findViewById(R.id.name);
        usName.setText("Identified As " + name + "!");

        TextView confidences = alert.findViewById(R.id.readings);
        confidences.setText("SVC - " + svcmatch + ". Confidence: " + ((int) (svcCon * 100)) + "%"
                + "\n" + "KNN - " + knnmatch + ". Confidence: " + ((int) (knnCon * 100)) + "%"
                + "\n" + "NN - " + nnmatch + ". Confidence: " + ((int) (nnCon * 100)) + "%"
                + "\n" + "DT - " + jmatch + ". Confidence: " + ((int) (dtCon * 100)) + "%");
        //TextView details = alert.findViewById(R.id.details);
        //details.setText(info);
        Button exit = alert.findViewById(R.id.done);
        Button stats = alert.findViewById(R.id.ugly);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                alert.cancel();
            }
        });
        final String statistics = info;
        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStats(statistics);
            }
        });

        alert.show();
    }

    //Show an Alert Dialog with extra stats/
    public void showStats(String info) {
        final Dialog alert = new Dialog(MainActivity.this);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.ugly_stats);
        alert.setCancelable(false);

        TextView display = alert.findViewById(R.id.readings);
        display.setText(info);

        Button back = alert.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                alert.cancel();
            }
        });

        alert.show();
    }

    //Recognize a user. Microsoft Face API AsyncTask.
    private String recognizeandFrame(final Bitmap mBitmap, final HashMap<String, String> hashMap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream((outputStream.toByteArray()));

        AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {
            ProgressDialog pd = new ProgressDialog(MainActivity.this);

            @Override
            protected Face[] doInBackground(InputStream... inputStreams) {

                publishProgress("Processing...");
                FaceServiceClient.FaceAttributeType[] faceAttr = new FaceServiceClient.FaceAttributeType[]{
                        FaceServiceClient.FaceAttributeType.HeadPose,
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Emotion,
                        FaceServiceClient.FaceAttributeType.FacialHair,
                        FaceServiceClient.FaceAttributeType.Hair
                };


                try {
                    Face[] result = faceServiceClient.detect(inputStreams[0],
                            true,
                            false,
                            faceAttr);

                    if (result == null) {
                        publishProgress("Detection failed. Nothing detected.");
                    }

                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    return result;
                } catch (Exception e) {
                    publishProgress("Detection Failed: " + e.getMessage());
                    return null;
                }
            }


            @Override
            protected void onPreExecute() {
                pd.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                pd.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Face[] faces) {
                pd.dismiss();
                if (faces == null || faces.length == 0) {
                    displayMessage("No face detected. Please retake the picture.");
                    Log.d("HERE------------------", "No faces detected. Please retake picture");
                    //longToast("Hi");
                } else {
                    age = Double.toString(faces[0].faceAttributes.age);
                    //  longToast("AGE: " + age);

                    double x = faces[0].faceAttributes.headPose.roll;
                    double z = faces[0].faceAttributes.headPose.yaw;

                    if ((x > -10 && x < 10) && (z < 10 && z > -10)) {
                        HashMap<String, String> temp = hashMap;
                        temp.put("Age", age);
                        makePrediction(temp);
                    } else {
                        displayMessage("Please retake the picture at a better angle.");
                    }

                    //progressDialog(temp);

                }
            }
        };
        detectTask.execute(inputStream);
        return age;
    }

    //Detect a user. Microsoft Face API AsyncTask.
    private String detectandFrame(final Bitmap mBitmap, final HashMap<String, String> hashMap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream((outputStream.toByteArray()));

        AsyncTask<InputStream, String, Face[]> detectTask = new AsyncTask<InputStream, String, Face[]>() {
            ProgressDialog pd = new ProgressDialog(MainActivity.this);

            @Override
            protected Face[] doInBackground(InputStream... inputStreams) {

                publishProgress("Processing...");
                FaceServiceClient.FaceAttributeType[] faceAttr = new FaceServiceClient.FaceAttributeType[]{
                        FaceServiceClient.FaceAttributeType.HeadPose,
                        FaceServiceClient.FaceAttributeType.Age,
                        FaceServiceClient.FaceAttributeType.Gender,
                        FaceServiceClient.FaceAttributeType.Emotion,
                        FaceServiceClient.FaceAttributeType.FacialHair,
                        FaceServiceClient.FaceAttributeType.Hair
                };


                try {
                    Face[] result = faceServiceClient.detect(inputStreams[0],
                            true,
                            false,
                            faceAttr);

                    if (result == null) {
                        publishProgress("Detection failed. Nothing detected.");
                    }

                    publishProgress(String.format("Detection Finished. %d face(s) detected", result.length));
                    return result;
                } catch (Exception e) {
                    publishProgress("Detection Failed: " + e.getMessage());
                    return null;
                }
            }


            @Override
            protected void onPreExecute() {
                pd.show();
            }

            @Override
            protected void onProgressUpdate(String... values) {
                pd.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Face[] faces) {
                pd.dismiss();
                if (faces == null || faces.length == 0) {
                    displayMessage("No face detected. Please retake the picture and at a better angle.");
                } else {
                    Hair.HairColor[] haircolors = faces[0].faceAttributes.hair.hairColor;
                    makeToast("Color: " + haircolors[0].color + "   Confidence: " + haircolors[0].confidence);
                    makeToast("Color: " + haircolors[1].color + "   Confidence: " + haircolors[1].confidence);
                    for (Hair.HairColor hair : haircolors) {
                        //        makeToast("Color: " + hair.color + "  Confidence: " + hair.confidence);
                    }
                    double x = faces[0].faceAttributes.headPose.roll;
                    double y = faces[0].faceAttributes.headPose.pitch;
                    double z = faces[0].faceAttributes.headPose.yaw;

                    if ((x > -10 && x < 10) && (z < 10 && z > -10)) {
                        age = Double.toString(faces[0].faceAttributes.age);
                        HashMap<String, String> temp = hashMap;
                        temp.put("Age", age);
                        saveData2(temp);
                    } else {
                        displayMessage("Please retake the picture at a better angle.");
                    }
                }
            }
        };
        detectTask.execute(inputStream);
        return age;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.item1:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IJ-Apps/Weka-ML-Face-Recognition-(Free!)"));
                startActivity(browserIntent);
                return true;
        }
        return true;
    }

    //Display Alert Dialog if there were any errors with registering or recognizing the face.
    public void displayMessage(String s) {
        final Dialog alert = new Dialog(MainActivity.this);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.customalert);
        alert.setCancelable(true);

        TextView text = alert.findViewById(R.id.message);
        text.setText(s);

        alert.show();
    }

    //Save the data in storage.
    private void saveData2(HashMap<String, String> hashMap) {
        BufferedReader inputReader = null;

        try {
            String yourFilePath = getApplicationContext().getFilesDir() + "/" + "faceData";
            File yourFile = new File(yourFilePath);
            inputReader = new BufferedReader(new FileReader(yourFile));

            String yourPath = getApplicationContext().getFilesDir() + "/" + "names";
            File file = new File(yourPath);
            BufferedReader namesReader = new BufferedReader(new FileReader(file));

            String line1 = "";
            String line2 = "";
            String tline;
            int mycount = 0;
            while ((tline = namesReader.readLine()) != null) {
                if (mycount == 0) {
                    line1 += tline;
                    mycount++;
                } else {
                    line2 += tline;
                }
            }
            //longToast("Lines in names: " + line1 + "\n" + line2);
            String[] namesarray = line1.split(" ");
            String[] numarray = line2.split(" ");


            HashMap<String, String> faceData = hashMap;

            StringBuilder sb = new StringBuilder();
            String line;
            String lines = "";
            float cheek = Float.parseFloat(faceData.get("Cheek diff"));
            float eye = Float.parseFloat(faceData.get("Eye diff"));
            float mouth = Float.parseFloat(faceData.get("Mouth diff"));
            float nose = Float.parseFloat(faceData.get("Nose diff"));
            String name = (faceData.get("Name"));
            double age = Float.parseFloat(faceData.get("Age"));

            int matchnum = 0;
            boolean contains = false;
            for (int i = 0; i < namesarray.length; i++) {
                if (namesarray[i].equals(name)) {
                    contains = true;
                    matchnum = i;
                    //makeToast("Matching Number: " + matchnum);
                    break;
                } else {
                    contains = false;
                    continue;
                }
            }

            int lastnum = Integer.parseInt(numarray[numarray.length - 1]);

            int count = 1;
            if (contains == false) {
                line1 += " " + name;
                line2 += " " + Integer.toString(lastnum + 1);
            }

            String[] d = line2.split("[ ,  ]");
            String tidy = "";
            for (int i = 1; i < d.length; i++) {
                tidy += d[i] + " ";
            }
            String allnames = "" + d[0] + "";
            for (int i = 1; i < d.length; i++) {
                allnames += ", " + d[i] + "";
                //      makeToast("Names: " + d[i]);
            }
            //makeToast("Allnames: " + allnames);
            while ((line = inputReader.readLine()) != null) {
                if (count == 7) {
                    lines += "@attribute class {" + allnames + "}\n";
                    // lines += "@data\n";
                } else {
                    sb.append(line);
                    lines += line + "\n";
                }
                count++;

            }

            if (contains == false) {
                //They are a new person
                lines += cheek + ", " + eye + ", " + mouth + ", " + nose + ", " + age + ", " + (lastnum + 1) + "";
            } else {
                lines += cheek + ", " + eye + ", " + mouth + ", " + nose + ", " + age + ", " + (matchnum) + "";
            }

            FileOutputStream outputStream;
            FileOutputStream writenames;

            names.clear();
            try {
                outputStream = openFileOutput("faceData", Context.MODE_PRIVATE);
                outputStream.write(lines.getBytes());
                outputStream.close();

                writenames = openFileOutput("names", Context.MODE_PRIVATE);
                writenames.write((line1 + "\n" + line2).getBytes());
                writenames.close();
                /*longToast("Made the file! " + lines);
                longToast(lines);*/
                longToast("Registered Person!");
            } catch (Exception e) {
                longToast("Message: " + e.getMessage() + " Cause: " + e.getCause());
                display.setText("Message: " + e.getMessage() + " Cause: " + e.getCause());
            }
        } catch (Exception e) {
            //Write the code to create the new file with all the tags for the first time.
            HashMap<String, String> faceData = hashMap;

            float cheek = Float.parseFloat(faceData.get("Cheek diff"));
            float eye = Float.parseFloat(faceData.get("Eye diff"));
            float mouth = Float.parseFloat(faceData.get("Mouth diff"));
            float nose = Float.parseFloat(faceData.get("Nose diff"));
            String name = (faceData.get("Name"));
            float age = Float.parseFloat(faceData.get("Age"));

            String relation = "@relation practice";
            String attributes = "\n@attribute cheek numeric\n" +
                    "@attribute eye numeric\n" +
                    "@attribute mouth numeric\n" +
                    "@attribute nose numeric\n" +
                    "@attribute age numeric\n" +
                    "@attribute class {0}";

            String lines = "\n" + cheek + ", " + eye + ", " + mouth + ", " + nose + ", " + age + ", 0";
            String data = "\n\n@data" + lines + "\n";
            FileOutputStream outputStream;
            FileOutputStream stream;
            String line = name + "\n" + "0";

            try {
                stream = openFileOutput("names", Context.MODE_PRIVATE);
                stream.write((line).getBytes());
                stream.close();
                //   makeToast("Made the file with names!");
            } catch (Exception ex) {
                longToast("Message: " + ex.getMessage() + " Cause: " + ex.getCause());
                display.setText("Message: " + ex.getMessage() + " Cause: " + ex.getCause());
            }

            try {
                outputStream = openFileOutput("faceData", Context.MODE_PRIVATE);
                outputStream.write((relation + attributes + data).getBytes());
                outputStream.close();
                makeToast("Made the file!");
            } catch (Exception ex) {
                longToast("Message: " + ex.getMessage() + " Cause: " + ex.getCause());
                display.setText("Message: " + ex.getMessage() + " Cause: " + ex.getCause());
            }

        }
    }

    private void longToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    //Detect when the camera is launched, either for recognition or registration.
    //Then get facial info like mouth, nose, cheek, and more.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        image.setImageBitmap(bitmap);
        if (requestCode == 1 && resultCode == RESULT_OK) {


            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder().
                            setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .setMinFaceSize(0.15f)
                            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                            .build();
            FirebaseVisionFaceDetector detector;

            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
            final FirebaseVisionImage image2 = FirebaseVisionImage.fromBitmap(bitmap);
            Task<List<FirebaseVisionFace>> result =
                    detector.detectInImage(image2)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            if (faces.isEmpty() || faces == null) {
                                                displayMessage("No face detected. Please retake the picture and at a better angle.");
                                                //Log.d("HERE------------------", "No faces detected. Please retake picture");
                                            } else {
                                                for (FirebaseVisionFace face : faces) {

                                                    Rect bounds = face.getBoundingBox();
                                                    int bot = bounds.bottom;
                                                    int top = bounds.top;
                                                    int right = bounds.right;
                                                    int left = bounds.left;
                                                    //makeToast(Float.toString(bot) + " " + Float.toString(top) + " " + Float.toString(right) + " " + Float.toString(left));


                                                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
                                                    int id = 0;
                                                    float smileProb = 0, rightEyeOpenProb = 0, leftEyeOpenProb = 0;
                                                    FirebaseVisionPoint leftEarPos, rightEarPos, moutPos, leftEyePos, rightEyePos;

                                                    int heightDif = Math.round(Math.abs(bot - top));
                                                    int lengthDif = Math.round(Math.abs(left - right));

                                                    HashMap<String, String> hashMap = new HashMap<>();
                                              /*  hashMap.put("Bottom", bot);
                                                hashMap.put("Top", top);
                                                hashMap.put("Right", right);
                                                hashMap.put("Left", left);*/

                                                    float leftcheekpos = 0;
                                                    float rightcheekpos = 0;
                                                    FirebaseVisionFaceLandmark leftcheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
                                                    FirebaseVisionFaceLandmark rightcheek = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);

                                                    if (leftcheek != null) {
                                                        leftcheekpos = leftcheek.getPosition().getX();
                                                    }

                                                    if (rightcheek != null) {
                                                        rightcheekpos = rightcheek.getPosition().getX();
                                                    }

                                                    int nosex = 0;
                                                    int nosey = 0;
                                                    FirebaseVisionFaceLandmark nosebase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);

                                                    if (nosebase != null) {
                                                        nosex = Math.round(nosebase.getPosition().getX());
                                                        nosey = Math.round(nosebase.getPosition().getY());
                                                    }
                                                    FirebaseVisionFaceLandmark mouthleft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                                                    FirebaseVisionFaceLandmark mouthright = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
                                                    float mouthleftx = 0;
                                                    float mouthrightx = 0;
                                                    if (mouthleft != null) {
                                                        mouthleftx = mouthleft.getPosition().getX();
                                                    }

                                                    if (mouthright != null) {
                                                        mouthrightx = mouthright.getPosition().getX();
                                                    }

                                                    FirebaseVisionFaceLandmark mouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                                                    int mouthX = 0;
                                                    int mouthY = 0;
                                                    int eyeY = 0;
                                                    if (mouth != null) {
                                                        moutPos = mouth.getPosition();
                                                        mouthX = Math.round(moutPos.getX());
                                                        mouthY = Math.round(moutPos.getY());
                                                        //makeToast("Mouth - " + moutPos.getX().toString());
                                                    } else {
                                                        // makeToast("No mouth.");
                                                    }
                                                    float leftEyep = 0;
                                                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                                    if (leftEye != null) {
                                                        leftEyePos = leftEye.getPosition();
                                                        //position.setText(position.getText().toString() + "\nLeft Eye Pos - " + leftEyePos.getX().toString());
                                                        leftEyep = leftEyePos.getX();
                                                        eyeY = Math.round(leftEyePos.getY());
                                                    }

                                                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                                    float rightEyep = 0;
                                                    if (rightEye != null) {
                                                        rightEyePos = rightEye.getPosition();
                                                        //position.setText(position.getText().toString() + "\nRight Eye Pos - " + rightEyePos.toString());
                                                        rightEyep = rightEyePos.getX();
                                                        eyeY = Math.round(Math.abs(Math.round(rightEyePos.getY()) - eyeY) / 2);

                                                    }

                                                    FirebaseVisionFaceLandmark nbase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                                    FirebaseVisionFaceLandmark mbase = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                                                    double leftdis, rightdis;
                                                    float leftx = nbase.getPosition().getX() - mouthleftx;
                                                    float lefty = nbase.getPosition().getY() - mouthleft.getPosition().getY();
                                                    float righty = nbase.getPosition().getY() - mouthright.getPosition().getY();
                                                    float rightx = nbase.getPosition().getX() - mouthrightx;


                                                    leftdis = Math.sqrt(Math.pow(leftx, 2) + Math.pow(lefty, 2));
                                                    rightdis = Math.sqrt(Math.pow(rightx, 2) + Math.pow(righty, 2));

                                                    leftdis = (100 * leftdis) / heightDif;
                                                    rightdis = (100 * rightdis) / heightDif;

                                                    double eyedisl, eyedisr;
                                                    float rightx2 = rightEye.getPosition().getX() - mouthrightx;
                                                    float rightxy = rightEye.getPosition().getY() - mouthright.getPosition().getY();
                                                    float lefttxy = leftEye.getPosition().getY() - mouthleft.getPosition().getY();
                                                    float leftx2 = leftEye.getPosition().getX() - mouthleftx;

                                                    eyedisl = Math.sqrt(Math.pow(lefttxy, 2) + Math.pow(leftx2, 2));
                                                    eyedisr = Math.sqrt(Math.pow(rightx2, 2) + Math.pow(rightxy, 2));

                                                    eyedisl = (100 * eyedisl) / heightDif;
                                                    eyedisr = (100 * eyedisr) / heightDif;
                                                    //display.setText(leftdis + " " + rightdis + " " + eyedisl + " " + eyedisr + " " + Double.toString((leftdis + rightdis) / 2).substring(0, 5) + " " + Double.toString((eyedisl + eyedisr) / 2).substring(0, 5));

                                              /*  hashMap.put("Left Eye-Mouth", Double.toString(eyedisl));
                                                hashMap.put("Right Eye-Mouth", Double.toString(eyedisr));
                                                hashMap.put("Left Nose-Mouth", Double.toString(leftdis));
                                                hashMap.put("Right Nose-Mouth", Double.toString(rightdis));*/
                                                    // hashMap.put("AVG Eye-Mouth", Double.toString((eyedisl + eyedisr) / 2));

                                                    //databaseReference.push().setValue(hashMap);

                                                    long a = Math.round(Math.sqrt(Math.pow(nosex, 2) + Math.pow(nosey, 2)));
                                                    float scale = 100;
                                                    float eyeDif = (100 * (rightEyep - leftEyep)) / ((heightDif + lengthDif) / 2);
                                                    float mouthDif = (100 * (mouthrightx - mouthleftx)) / ((heightDif + lengthDif) / 2);
                                                    float cheekDif = (100 * (rightcheekpos - leftcheekpos)) / ((heightDif + lengthDif) / 2);

                                                    hashMap.put("Eye diff", Float.toString(eyeDif));
                                                    hashMap.put("Height diff", Integer.toString(heightDif) + ".0");
                                                    hashMap.put("Cheek diff", Float.toString(cheekDif));

                                                    hashMap.put("Length diff", Integer.toString(lengthDif) + ".0");
                                                    hashMap.put("Mouth diff", Float.toString(mouthDif));
                                                    hashMap.put("Name", name.getText().toString());
                                                    hashMap.put("Nose diff", Double.toString((leftdis + rightdis) / 2).substring(0, 9));
                                                    BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
                                                    Bitmap bitmap = drawable.getBitmap();
                                                    detectandFrame(bitmap, hashMap);
                                                    //  saveData2(hashMap);

                                                    // display.setText(hashMap.toString());

                                                    // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                    // nose available):


                                             /*   display.setText("ID: " + Integer.toString(id) + "     Smiling: " + smileProbability + "\nLeft Eye Open: " + lefteye + "\nRight Eye Open: " + righteye
                                                        + "\nTilts:   Right/Left:  " + sidetilt + "  Sideways:  " + sidewaytilt);*/

                                                }
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            displayMessage("No faces detected.");
                                            makeToast("IT FAILED!");
                                            makeToast(e.getMessage());
                                            makeToast(e.getMessage());
                                            makeToast(e.getMessage());
                                        }
                                    });
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            /*Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(bitmap);*/

            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder().
                            setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .setMinFaceSize(0.15f)
                            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                            .build();

            FirebaseVisionFaceDetector detector;

            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
            FirebaseVisionImage image2 = FirebaseVisionImage.fromBitmap(bitmap);
            Task<List<FirebaseVisionFace>> result =
                    detector.detectInImage(image2)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            if (faces.isEmpty() || faces == null) {
                                                displayMessage("No face detected. Please retake the picture and at a better angle.");
                                                //Log.d("HERE------------------", "No faces detected. Please retake picture");
                                            } else {
                                                for (FirebaseVisionFace face : faces) {

                                                    List<FirebaseVisionPoint> leftEyeContour =
                                                            face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
                                                    List<FirebaseVisionPoint> upperLipBottomContour =
                                                            face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();
                                                    // information.setText(leftEyeContour.toString());
                                                    // makeToast(leftEyeContour.toString() + " " + upperLipBottomContour.toString());

                                                    Rect bounds = face.getBoundingBox();
                                                    int bot = bounds.bottom;
                                                    int top = bounds.top;
                                                    int right = bounds.right;
                                                    int left = bounds.left;
                                                    //makeToast(Float.toString(bot) + " " + Float.toString(top) + " " + Float.toString(right) + " " + Float.toString(left));


                                                    float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                    float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
                                                    int id = 0;
                                                    float smileProb = 0, rightEyeOpenProb = 0, leftEyeOpenProb = 0;
                                                    FirebaseVisionPoint leftEyePos, rightEyePos;

                                                    int heightDif = Math.round(Math.abs(bot - top));
                                                    int lengthDif = Math.round(Math.abs(left - right));

                                                    HashMap<String, String> hashMap = new HashMap<>();


                                                    float leftcheekpos = 0;
                                                    float rightcheekpos = 0;
                                                    FirebaseVisionFaceLandmark leftcheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
                                                    FirebaseVisionFaceLandmark rightcheek = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR);

                                                    if (leftcheek != null) {
                                                        leftcheekpos = leftcheek.getPosition().getX();
                                                    }

                                                    if (rightcheek != null) {
                                                        rightcheekpos = rightcheek.getPosition().getX();
                                                    }

                                                    int nosex = 0;
                                                    int nosey = 0;

                                                    FirebaseVisionFaceLandmark mouthleft = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT);
                                                    FirebaseVisionFaceLandmark mouthright = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT);
                                                    float mouthleftx = 0;
                                                    float mouthrightx = 0;
                                                    if (mouthleft != null) {
                                                        mouthleftx = mouthleft.getPosition().getX();
                                                    }

                                                    if (mouthright != null) {
                                                        mouthrightx = mouthright.getPosition().getX();
                                                    }


                                                    float leftEyep = 0;
                                                    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                                    if (leftEye != null) {
                                                        leftEyePos = leftEye.getPosition();
                                                        //position.setText(position.getText().toString() + "\nLeft Eye Pos - " + leftEyePos.getX().toString());
                                                        leftEyep = leftEyePos.getX();
                                                        //  eyeY = Math.round(leftEyePos.getY());
                                                    }

                                                    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                                    float rightEyep = 0;
                                                    if (rightEye != null) {
                                                        rightEyePos = rightEye.getPosition();
                                                        //position.setText(position.getText().toString() + "\nRight Eye Pos - " + rightEyePos.toString());
                                                        rightEyep = rightEyePos.getX();
                                                        //  eyeY = Math.round(Math.abs(Math.round(rightEyePos.getY()) - eyeY) / 2);

                                                    }

                                                    FirebaseVisionFaceLandmark nbase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                                    FirebaseVisionFaceLandmark mbase = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM);
                                                    double leftdis, rightdis;
                                                    float leftx = nbase.getPosition().getX() - mouthleftx;
                                                    float lefty = nbase.getPosition().getY() - mouthleft.getPosition().getY();
                                                    float righty = nbase.getPosition().getY() - mouthright.getPosition().getY();
                                                    float rightx = nbase.getPosition().getX() - mouthrightx;

                                                    leftdis = Math.sqrt(Math.pow(leftx, 2) + Math.pow(lefty, 2));
                                                    rightdis = Math.sqrt(Math.pow(rightx, 2) + Math.pow(righty, 2));

                                                    leftdis = (100 * leftdis) / heightDif;
                                                    rightdis = (100 * rightdis) / heightDif;

                                                    long a = Math.round(Math.sqrt(Math.pow(nosex, 2) + Math.pow(nosey, 2)));
                                                    float scale = 100;
                                                    float eyeDif = (100 * (rightEyep - leftEyep)) / ((heightDif + lengthDif) / 2);
                                                    float mouthDif = (100 * (mouthrightx - mouthleftx)) / ((heightDif + lengthDif) / 2);
                                                    float cheekDif = (100 * (rightcheekpos - leftcheekpos)) / ((heightDif + lengthDif) / 2);

                                                    hashMap.put("Eye diff", Float.toString(eyeDif));
                                                    hashMap.put("Height diff", Integer.toString(heightDif) + ".0");
                                                    hashMap.put("Length diff", Integer.toString(lengthDif) + ".0");
                                                    hashMap.put("Mouth diff", Float.toString(mouthDif));
                                                    hashMap.put("Cheek diff", Float.toString(cheekDif));
                                                    hashMap.put("Nose diff", Double.toString((leftdis + rightdis) / 2).substring(0, 9));
                                                    BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
                                                    Bitmap bitmap = drawable.getBitmap();

                                                    recognizeandFrame(bitmap, hashMap);


                                                    //makeToast(hashMap.toString());

                                                }
                                            }
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            displayMessage("No faces detected.");
                                            makeToast("IT FAILED!");
                                            makeToast(e.getMessage());
                                        }
                                    });
        }


    }

}
