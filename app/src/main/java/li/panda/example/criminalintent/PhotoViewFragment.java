package li.panda.example.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.UUID;

/**
 * Created by xueli on 2016/6/3.
 */
public class PhotoViewFragment extends DialogFragment {

    private static final String PHOTO_SHOW = "photo";

    private ImageView imageView;
    private Crime crime;
    private File file;

    public static PhotoViewFragment newInstance(UUID uuid) {
        Bundle args = new Bundle();
        args.putSerializable(PHOTO_SHOW, uuid);

        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.photo_view, null);
        imageView = (ImageView) view.findViewById(R.id.iv_photo);
        UUID uuid = (UUID) getArguments().getSerializable(PHOTO_SHOW);
        crime = CrimeLab.getCrimeLab(getActivity()).getCrime(uuid);
        file = CrimeLab.getCrimeLab(getActivity()).getPhotoFile(crime);
        if (file != null) {
            Bitmap bitmap = PictureUtils.getScaledBitmap(file.getPath(), getActivity());
            imageView.setImageBitmap(bitmap);
        }
        return new AlertDialog.Builder(getActivity()).setTitle("图片详情").setPositiveButton(android
                .R.string.ok, null).setView(view).create();
    }
}
