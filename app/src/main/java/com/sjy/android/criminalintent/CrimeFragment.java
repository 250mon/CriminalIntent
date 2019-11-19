package com.sjy.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Created by pc on 2017-07-23.
 */

public class CrimeFragment extends Fragment {
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String KEY_NUMBER = "phone_number";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int PERMISSION_REQUEST_CODE = 99;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSuspectButton;
    private Button mReportButton;
    private Button mCallButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    // Why not a constructor with crimeId argument?
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * Receiving menu callback from the activity from the os
         * with the help of the fragment manager
         **/
        setHasOptionsMenu(true);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

        /* Get a permission to access the contacts */
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.READ_CONTACTS};
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Permission success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Permission failure", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                mCrime.setTitle(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate().toString());
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());

                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the crime's solved property
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                 * Intent i = new Intent(Intent.ACTION_SEND);
                 * i.setType("text/plain");
                 * i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                 * i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                 * i = Intent.createChooser(i, getString(R.string.send_report));
                 */
                Intent i = IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .createChooserIntent();
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }
        // Guarding against no contact app
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mCallButton = (Button) v.findViewById(R.id.make_call);
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri numberUri = Uri.parse("tel:" + mCrime.getPhoneNumber());
                Intent i = new Intent(Intent.ACTION_DIAL, numberUri);
                startActivity(i);
            }
        });
        if (mCrime.getPhoneNumber() == null) {
            mCallButton.setEnabled(false);
        }

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // check if there is an external storage to put images in and the camera app is available
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                String authority = getActivity().getApplicationContext().getPackageName() + ".fileprovider";
                uri = FileProvider.getUriForFile(getActivity(), authority, mPhotoFile);
                captureImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(mPhotoFile);
            }
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        return v;
    }

    /*
     * Inflate a menu resource
     * - delete menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                Intent intent = new Intent(getActivity(), CrimeListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE && data != null) {
            Instant date = (Instant) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver().query(
                    contactUri,
                    queryFields,
                    null,
                    null,
                    null
            );

            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String contactId = c.getString(0);
                String suspect = c.getString(1);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                if (Integer.parseInt(c.getString(2)) > 0) {
                    mCallButton.setEnabled(true);
                    mCrime.setPhoneNumber(retrievePhoneNumber(contactId));
                } else {
                    mCallButton.setEnabled(false);
                }
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
        }
    }

    private String retrievePhoneNumber(String contactId) {
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor crPhones = getActivity().getContentResolver().query(
                contactUri,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );

        try {
            if (crPhones.getCount() == 0) {
                return null;
            }
            crPhones.moveToFirst();
            String phoneNumber = crPhones.getString(crPhones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            return phoneNumber;
        } finally {
            crPhones.close();
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        /*String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();*/
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE-MMM-dd").withZone(ZoneId.systemDefault());
        String dateString = formatter.format(mCrime.getDate());

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null ||!mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

}
