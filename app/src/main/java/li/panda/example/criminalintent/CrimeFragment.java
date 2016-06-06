package li.panda.example.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by xueli on 2016/5/30.
 */
public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_PHOTO = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;


    private Crime crime;
    private EditText titleField;
    private Button dateButton;
    private CheckBox solvedCheckBox;
    private Button btn_send_message;
    private Button btn_choose_person;
    private ImageButton photoButton;
    private ImageView photoView;
    private File photoFile;
    private Callbacks callbacks;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");


    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle arg = new Bundle();
        arg.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(arg);
        return fragment;
    }

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        crime = CrimeLab.getCrimeLab(getActivity()).getCrime(crimeId);
        photoFile = CrimeLab.getCrimeLab(getActivity()).getPhotoFile(crime);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        //Intent intent = new Intent();
        //intent.putExtra("notify", crime.getId());
        //getActivity().setResult(Activity.RESULT_OK, intent);
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        titleField = (EditText) v.findViewById(R.id.crime_title);
        dateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        //dateButton.setEnabled(false);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                DatePickerFragment fragment = DatePickerFragment.newInstance(crime.getDate());
                fragment.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                fragment.show(fragmentManager, DIALOG_DATE);
            }
        });
        solvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        titleField.setText(crime.getTitle());
        solvedCheckBox.setChecked(crime.isSolved());
        solvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                crime.setSolved(isChecked);
                updateCrime();
            }
        });
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_send_message = (Button) v.findViewById(R.id.btn_send_message);
        btn_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, "陋习应用记录");
                i = Intent.createChooser(i, "发送陋习信息");
                startActivity(i);
            }
        });
        final Intent pickContent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts
                .CONTENT_URI);
        btn_choose_person = (Button) v.findViewById(R.id.btn_choose_person);
        btn_choose_person.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContent, REQUEST_CONTACT);
            }
        });
        if (crime.getPerson() != null) {
            btn_choose_person.setText(crime.getPerson());
        }
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContent, PackageManager.MATCH_DEFAULT_ONLY) ==
                null) {
            btn_choose_person.setEnabled(false);
        }
        photoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImg = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = photoFile != null && captureImg.resolveActivity(packageManager) !=
                null;
        if (canTakePhoto) {
            Uri uri = Uri.fromFile(photoFile);
            captureImg.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImg, REQUEST_PHOTO);
            }
        });
        photoView = (ImageView) v.findViewById(R.id.crime_photo);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                PhotoViewFragment fragment = PhotoViewFragment.newInstance(crime.getId());
                fragment.show(fragmentManager, DIALOG_PHOTO);
            }
        });
        updatePhotoView();
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            crime.setDate(date);
            updateCrime();
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            String[] queryFiles = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = getActivity().getContentResolver().query(contactUri, queryFiles,
                    null, null, null);
            try {
                if (cursor.getCount() == 0) {
                    return;
                }
                cursor.moveToFirst();
                String person = cursor.getString(0);
                crime.setPerson(person);
                updateCrime();
                btn_choose_person.setText(person);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            updateCrime();
            updatePhotoView();
        }
    }

    private void updateCrime() {
        CrimeLab.getCrimeLab(getActivity()).updateCrime(crime);
        callbacks.onCrimeUpdated(crime);
    }

    private void updateDate() {
        dateButton.setText(dateFormat.format(crime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = "该陋习已解决";
        } else {
            solvedString = "该陋习还未解决";
        }
        String person = crime.getPerson();
        if (person == null) {
            person = "未记录人员";
        } else {
            person = "该人员为" + person;
        }
        String report = getString(R.string.crime_report, crime.getTitle(), dateFormat.format
                (crime.getDate()), solvedString, person);
        return report;
    }

    private void updatePhotoView() {
        if (photoFile == null || !photoFile.exists()) {
            photoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());
            photoView.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.getCrimeLab(getActivity()).updateCrime(crime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }
}
