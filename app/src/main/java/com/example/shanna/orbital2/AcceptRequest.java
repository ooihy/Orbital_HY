package com.example.shanna.orbital2;

import android.app.PendingIntent;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.PendingIntent.getActivity;
import static android.app.PendingIntent.readPendingIntentOrNullFromParcel;
import static android.app.PendingIntent.writePendingIntentOrNullToParcel;

public class AcceptRequest extends AppCompatActivity {

    private TextView mPay;
    private TextView mDuration;
    private TextView mBufferWait;
    private TextView mMaxChanges;
    private TextView mDateOfRequest;
    private TextView mTitle;

    private TextView mBtnProfileView;
    private Button mBtnReject;
    private Button mBtnAccept;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseClone;
    private DatabaseReference mRequesterDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_request);

        //From requestFragment
        mPay = findViewById(R.id.textViewPay);
        mDuration = findViewById(R.id.textViewDuration);
        mBufferWait = findViewById(R.id.textViewWait);
        mMaxChanges = findViewById(R.id.textViewChanges);
        mDateOfRequest = findViewById(R.id.textViewFormSubmitDate);
        mTitle = findViewById(R.id.textViewTitle);

        mBtnProfileView=findViewById(R.id.buttonViewRequesterProfile);
        mBtnReject = findViewById(R.id.buttonReject);
        mBtnAccept = findViewById(R.id.buttonAccept);

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        final String owner_id = current_user.getUid();

        final String pay = getIntent().getStringExtra("pay");
        final String duration = getIntent().getStringExtra("duration");
        final String bufferWait = getIntent().getStringExtra("bufferWait");
        final String maxChanges = getIntent().getStringExtra("maxChanges");
        final String requestDate = getIntent().getStringExtra("requestDate");
        final String partnerID = getIntent().getStringExtra("partnerID");
        final String senderFullName = getIntent().getStringExtra("senderFullName");
        final String project_title = getIntent().getStringExtra("projectTitle");

       // Toast.makeText(AcceptRequest.this, "sender id is ->" + senderID, Toast.LENGTH_SHORT).show();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // display agreement details by reading from the AllCollabsReq branch
        mDatabase.child("AllCollabsReq").child(owner_id)
                .child(project_title + partnerID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        mPay.setText(dataSnapshot.child("Pay").getValue().toString());
                        mDuration.setText(dataSnapshot.child("Duration").getValue().toString());
                        mBufferWait.setText(dataSnapshot.child("BufferWait").getValue().toString());
                        mMaxChanges.setText(dataSnapshot.child("MaxChanges").getValue().toString());
                        mDateOfRequest.setText(dataSnapshot.child("DateOfRequest").getValue().toString());
                        mTitle.setText(dataSnapshot.child("Title").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

        //bring user to see the requester's profile
        mBtnProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AcceptRequest.this, ViewProfile.class);
                intent.putExtra("user_id", partnerID);
                startActivity(intent);
                finish();
            }
        });



        mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //1) Make a new branch under successfulCollaborations -> Same level as Users.
                mDatabaseClone = FirebaseDatabase.getInstance().getReference().child("SuccessfulCollaborations").child(owner_id)
                        .child(project_title + partnerID);

                ////////////////1) update the branch called SuccessfulCollaboration -> Same level as Users////
                final HashMap<String, Object> collabMapClone = new HashMap<>();
                collabMapClone.put("Pay", pay);
                collabMapClone.put("Duration", duration);
                collabMapClone.put("BufferWait", bufferWait);
                collabMapClone.put("MaxChanges", maxChanges);
                collabMapClone.put("DateOfRequest", requestDate);
                collabMapClone.put("Partner", FirebaseAuth.getInstance().getCurrentUser().getUid());
                collabMapClone.put("SenderFullName", senderFullName);
                collabMapClone.put("Title", project_title);
                mDatabaseClone.setValue(collabMapClone);

                ////////////////////////////////////////////////////////////////////////////////////////
                //2) Update projects collaboration details for the project owner.

                //I think cannot do hashmap for below because some of the fields are already present, eg projectsummary
                //Need to point out the fields directly. Because if use hashmap, then some fields like project qualifications/summary will get
                //erased
                //Note that the project owner can only accept one freelancer request, otherwise only the latest user's data who got accepted will
                //be stored

                DatabaseReference mOwnerDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users")
                                .child(owner_id)
                                .child("Projects")
                                .child(project_title) //project title
                            //    .child(partnerID)
                                .child("Pay");

                mOwnerDatabase1.setValue(pay);

                DatabaseReference mOwnerDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                      //  .child(partnerID)
                        .child("DateOfRequest");
                mOwnerDatabase2.setValue(requestDate);

                DatabaseReference mOwnerDatabase3 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                        .child("Duration");
                mOwnerDatabase3.setValue(duration);

                DatabaseReference mOwnerDatabase4 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                    //    .child(partnerID)
                        .child("MaxChanges");
                mOwnerDatabase4.setValue(maxChanges);

                DatabaseReference mOwnerDatabase5 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                     //   .child(partnerID)
                        .child("Partner");
                mOwnerDatabase5.setValue(partnerID);

                DatabaseReference mOwnerDatabase6 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                      //  .child(partnerID)
                        .child("Pay");
                mOwnerDatabase6.setValue(pay);

                DatabaseReference mOwnerDatabase7 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                    //    .child(partnerID)
                        .child("ProjectStatus");
                mOwnerDatabase7.setValue("Closed. I have found a partner to work the project with me.");


                DatabaseReference mOwnerDatabase8 = FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(owner_id)
                        .child("Projects")
                        .child(project_title) //project title
                        //    .child(partnerID)
                        .child("BufferWait");
                mOwnerDatabase8.setValue(bufferWait);

                ///////////////////////////////////////////////////////////////////////////////////////////////////


                //3) DOESN"T WORK -> THE APP CRASHES
                // delete the request from the request page of the project's owner
                //by deleting the branch from "AllCollabsReq" so that the request fragment
                //cannot find the branch and it will be empty.

                /*
                DatabaseReference mDeleteRequest = FirebaseDatabase.getInstance().getReference().child("AllCollabsReq")
                        .child(owner_id)
                        .child(project_title+partnerID);

                mDeleteRequest.removeValue();
                */

                //4) update for requester side so that it gets displayed under his/her projects branch

                //Need to first obtain Projectsummary, qualifications, responsibilities, dateoflisting from project owner's branch
                //Need to do datachange in order to obtain the values from the key
                DatabaseReference mgetDatabase = FirebaseDatabase.getInstance().getReference();

               mgetDatabase.child("Users").child(owner_id)
                        .child("Projects")
                        .child(project_title)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                String projectSummary = dataSnapshot.child("ProjectSummary").getValue().toString();
                                String projectQualifications = dataSnapshot.child("ProjectQualifications").getValue().toString();
                                String projectResponsibilities = dataSnapshot.child("ProjectResponsibilities").getValue().toString();
                                String projectDateOfListing = dataSnapshot.child("DateOfListing").getValue().toString();

                                mRequesterDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(partnerID)
                                        .child("Projects")
                                        .child(project_title);


                                final HashMap<String, Object> collabMapRequester = new HashMap<>();

                                collabMapRequester.put("Pay", pay);
                                collabMapRequester.put("Duration", duration);
                                collabMapRequester.put("BufferWait", bufferWait);
                                collabMapRequester.put("MaxChanges", maxChanges);
                                collabMapRequester.put("DateOfRequest", requestDate);
                                collabMapRequester.put("Owner", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                collabMapRequester.put("Partner", partnerID);
                                collabMapRequester.put("Title", project_title);
                                collabMapRequester.put("ProjectStatus", "Closed. I am currently working for the project owner.");
                                collabMapRequester.put("ProjectSummary", projectSummary);
                                collabMapRequester.put("ProjectQualifications", projectQualifications);
                                collabMapRequester.put("ProjectResponsibilities", projectResponsibilities);
                                collabMapRequester.put("DateOfListing", projectDateOfListing);

                                mRequesterDatabase.setValue(collabMapRequester);


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });


                ///////////////////////////////////////////////////////////////////////////////////////////////////
                Intent intent = new Intent(AcceptRequest.this, MainActivity.class);
                startActivity(intent);
                finish();


            }
        });





    }

}