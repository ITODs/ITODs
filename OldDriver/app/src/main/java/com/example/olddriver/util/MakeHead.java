package com.example.olddriver.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.olddriver.bean.UserBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * Created by 苏颂贤 on 2016/12/6.
 */

public class MakeHead {

    private String url;
    public static final int CHOOSE_PICTURE = 0;
    public static final int TAKE_PICTURE = 1;
    private static final int CROP_SMALL_PICTURE = 2;
    protected static Uri tempUri;

    private Activity activity;
    private ImageView head;

    public MakeHead(ImageView img_photo) {
        head = img_photo;
    }

    public void ChoosePic(final Activity activity, int which) {
        this.activity = activity;
        switch (which) {
            case CHOOSE_PICTURE: // 选择本地照片
                Intent openAlbumIntent = new Intent(
                        Intent.ACTION_GET_CONTENT);
                openAlbumIntent.setType("image/*");
                activity.startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
                break;
            case TAKE_PICTURE: // 拍照
                Intent openCameraIntent = new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE);
                tempUri = Uri.fromFile(new File(DataStore.USER_PATH + File.separator + BmobUser.getCurrentUser().getUsername(), "image.png"));
                // 指定照片保存路径（SD卡），image.png为一个临时文件，每次拍照后这个图片都会被替换
                openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                activity.startActivityForResult(openCameraIntent, TAKE_PICTURE);
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) { // 如果返回码是可以用的
            switch (requestCode) {
                case TAKE_PICTURE:
                    startPhotoZoom(tempUri); // 开始对图片进行裁剪处理
                    break;
                case CHOOSE_PICTURE:
                    startPhotoZoom(data.getData()); // 开始对图片进行裁剪处理
                    break;
                case CROP_SMALL_PICTURE:
                    if (data != null) {
                        try {
                            setImageToView(data); // 让刚才选择裁剪得到的图片显示在界面上
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     */
    protected void startPhotoZoom(Uri uri) {
        if (uri == null) {
            Log.i("tag", "The uri is not exist.");
        }
        tempUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    /**
     * 保存裁剪之后的图片数据
     */
    protected void setImageToView(Intent data) throws IOException {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            photo = toRoundBitmap(photo); // 这个时候的图片已经被处理成圆形的了
            head.setImageBitmap(photo);
            uploadPic(photo);
        }
    }


    private void uploadPic(Bitmap bitmap) throws IOException {
        // 上传至服务器
        // ... 可以在这里把Bitmap转换成file，然后得到file的url，做文件上传操作
        // bitmap是没有做个圆形处理的，但已经被裁剪了
        final BmobUser user = BmobUser.getCurrentUser();

        String imagePath = DataStore.USER_PATH + File.separator + user.getUsername()
                + File.separator + BmobUser.getCurrentUser().getUsername() + ".png";
        final File file = new File(imagePath);
        if (!file.exists()) file.createNewFile();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (fos != null) fos.close();
        }
        final BmobFile file2 = new BmobFile(new File(imagePath));
        file2.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    url = file2.getFileUrl();
                    UserBean newUser = new UserBean();
                    newUser.setHead_path(url);
                    newUser.update(user.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                                Toast.makeText(activity, "更换头像成功", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                }
            }
        });
    }

    public static Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bitmap;
    }

    /**
     * 转换图片成圆形
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public Bitmap toRoundBitmap(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
        if (width <= height) {
            roundPx = width / 2 -5;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2 -5;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
        final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
        final RectF rectF = new RectF(dst_left+15, dst_top+15, dst_right-20, dst_bottom-20);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }
}

