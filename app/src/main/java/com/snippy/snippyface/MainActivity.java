package com.snippy.snippyface;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private Button faceDetectButton,changefacing;
    private GraphicOverlay graphicOverlay;
    private CameraView cameraView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceDetectButton=findViewById(R.id.face_detection_btn);
        graphicOverlay=findViewById(R.id.graphic_overlay);
        cameraView=findViewById(R.id.camera_view);
        imageView = findViewById(R.id.image);
        changefacing = findViewById(R.id.change_facing);
        cameraView.setFacing(CameraKit.Constants.FACING_BACK);
        if(cameraView.getFacing()==CameraKit.Constants.FACING_BACK)
        {
            changefacing.setText("Open front cam");
        }
        else
        {
            changefacing.setText("Open Rear cam");
        }


        changefacing.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(cameraView.getFacing()==CameraKit.Constants.FACING_BACK)
                {
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    changefacing.setText("change to rear");
                }
                else
                {
                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    changefacing.setText("change to Front");
                }
            }
        });
        faceDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap=cameraKitImage.getBitmap();
                bitmap =Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();



                processFaceDetection(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

    }
    private void processFaceDetection(Bitmap bitmap) {

        FirebaseVisionImage firebaseVisionImage=FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions firebaseVisionFaceDetectorOptions=new FirebaseVisionFaceDetectorOptions.Builder().build();

        FirebaseVisionFaceDetector firebaseVisionFaceDetector= FirebaseVision.getInstance()
                .getVisionFaceDetector(firebaseVisionFaceDetectorOptions);

        firebaseVisionFaceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {

                        getFaceResults(firebaseVisionFaces);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Error"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getFaceResults(List<FirebaseVisionFace> firebaseVisionFaces) {
        int counter = 0;
        for (FirebaseVisionFace face:firebaseVisionFaces){
            Rect rect= face.getBoundingBox();
            RectOverlay rectOverlay= new RectOverlay(graphicOverlay,rect);

            graphicOverlay.add(rectOverlay);
            counter=counter+1;

        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraView.start();
    }



}