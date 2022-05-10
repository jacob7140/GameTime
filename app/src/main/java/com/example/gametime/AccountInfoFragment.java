package com.example.gametime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountInfoFragment extends Fragment {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public AccountInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private EditText editTextFN, editTextEmail, editTextPassword, editTextConfirmPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);

        editTextFN = view.findViewById(R.id.editTextAccountInfoFN);
        editTextEmail = view.findViewById(R.id.editTextAccountInfoEmail);
        editTextPassword = view.findViewById(R.id.editTextAccountInfoPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextAccountInfoConfirmPassword);

        editTextFN.setText(user.getDisplayName());
        editTextEmail.setText(user.getEmail());

        view.findViewById(R.id.buttonAccountInfoSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Are you sure you want to make these changes?");
                builder.setMessage("By confirming, changes to your account will be made!");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String FN = editTextFN.getText().toString();
                                String email = editTextEmail.getText().toString();
                                String password = editTextPassword.getText().toString();
                                String confirmPassword = editTextConfirmPassword.getText().toString();

                                if (FN.isEmpty()) {
                                    Toast.makeText(getActivity(), "First name field can't be empty.", Toast.LENGTH_SHORT).show();
                                } else if(!user.getDisplayName().equals(FN)) {
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(FN).build();
                                    user.updateProfile(profileUpdates);
                                }

                                if (email.isEmpty()) {
                                    Toast.makeText(getActivity(), "Email field can't be empty.", Toast.LENGTH_SHORT).show();
                                } else if(!user.getEmail().equals(email)) {
                                    user.updateEmail(email);
                                }

                                if (password.length() > 0 && !password.equals(confirmPassword)) {
                                    Toast.makeText(getActivity(), "Passwords must match", Toast.LENGTH_SHORT).show();
                                } else if (password.length() > 0 && password.length() < 6) {
                                    Toast.makeText(getActivity(), "Password must be longer than 5 characters.", Toast.LENGTH_SHORT).show();
                                } else if (password.length() > 0) {
                                    user.updatePassword(password);
                                }

                                mListener.goToProfile();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //find view AccountInfoDelete button and set as clickable button
        view.findViewById(R.id.buttonAccountInfoDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Declared AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("Are you sure you want to delete your account?"); //Title
                builder.setMessage("Enter your password and click confirm. Warning: Your account is not recoverable after deletion and will be lost forever!");//Message
                //declared editText for password input
                EditText editTextPassword = new EditText(getContext());
                editTextPassword.setHint("Password"); //set hint as "Password"
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());//set transformation to hidden the password with dots
                builder.setView(editTextPassword); //add editTextPassword to builder

                //DialogAlert clickable confirm button
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //re-authenticates the account to replace expired token with a new token
                        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), editTextPassword.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){ //checks if the task was successful
                                    //Then delete user account
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //create new activity, clear previous activity,and start a new activity at MainActivity.class.
                                                startActivity(new Intent(getActivity(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                //send message on screen
                                                Toast.makeText(getActivity(), "Account Deleted", Toast.LENGTH_SHORT).show();
                                            } else {
                                                //send message on screen
                                                Toast.makeText(getActivity(), "Failed to Delete Account, try again later!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(getActivity(), "Wrong Password! Try Again!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                //DialogAlert clickable cancel button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            };
        });

        view.findViewById(R.id.buttonAccountInfoBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToProfile();
            }
        });

        return view;
    }

    AccountInfoListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (AccountInfoListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RegisterListener");
        }
    }

    interface AccountInfoListener{
        void goToProfile();
    }
}