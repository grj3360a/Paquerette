package fr.iut.paquerette;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTakePhoto(View v){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView img = findViewById(R.id.imageView);
            img.setImageBitmap(imageBitmap);

            imageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);

            final TextView nbr = findViewById(R.id.nbrTextView);
            final TextView info = findViewById(R.id.infoTextView);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);

            /*
            LABELING
             */
            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            String s = "";
                            for(FirebaseVisionImageLabel label : labels){
                                s += label.getText() + " " + label.getConfidence() + "\n";
                            }
                            info.setText(s);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });

            /*
            OBJECTS
             */

            FirebaseVisionObjectDetectorOptions options =
                    new FirebaseVisionObjectDetectorOptions.Builder()
                            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                            .enableMultipleObjects()
                            .enableClassification()
                            .build();

            FirebaseVisionObjectDetector objectDetector =
                    FirebaseVision.getInstance().getOnDeviceObjectDetector(options);

            objectDetector.processImage(image)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<FirebaseVisionObject>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
                                    int nFleur = 0;
                                    for(FirebaseVisionObject object : detectedObjects){
                                        switch(object.getClassificationCategory()){

                                            case  FirebaseVisionObject.CATEGORY_PLANT:
                                                Log.i("Object", "PLANT " + object.getClassificationConfidence());
                                                nFleur++;
                                                nbr.setText(nFleur + " fleurs");
                                                break;

                                            default:
                                                Log.i("Object", object.getClassificationCategory() + " " + object.getClassificationConfidence());
                                                break;

                                        }
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });


        }
    }

}
