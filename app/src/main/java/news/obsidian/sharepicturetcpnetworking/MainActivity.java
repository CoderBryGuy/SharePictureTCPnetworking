package news.obsidian.sharepicturetcpnetworking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.SocketHandler;

public class MainActivity extends AppCompatActivity {
 static final int SNAP_PICTURE = 1;
    Uri outputFileUri;
    File file;
    int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState != null){
            outputFileUri = Uri.parse(savedInstanceState.getString("outputFileUri"));
            file = new File(savedInstanceState.getString("file"));
        }
    }

    public void btnSnapPic(View view) {
        counter++;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        Log.d("bryan", externalStorageDirectory.toString() );
        file = new File(externalStorageDirectory, "MyPhoto"+counter+".jpg");
        outputFileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, SNAP_PICTURE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("outputFileUri", outputFileUri.toString());
        outState.putString("file", file.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       if(requestCode == SNAP_PICTURE && resultCode == RESULT_OK){
       Log.d("bryan", "file saved in " + outputFileUri.toString());
       }
       // Bitmap bitmap = null;
        String s = null;
        if(file.exists()){
            InputStream inputStream;
          //  File externalStorageDirectory = Environment.getExternalStorageDirectory();
            try {
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int actuallyRead;
                while((actuallyRead = inputStream.read(buffer)) != -1){
                  s = new String(buffer, 0 , actuallyRead);
                 // bitmap = BitmapFactory.decodeFile(s);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute(s);
    }

 class MyAsyncTask extends AsyncTask<String, Void, String>{
     @Override
     protected String doInBackground(String... params) {
        String bitMapString = params[0];
         OutputStream outputStream =  null;
         InputStream inputStream = null;
         String result = null;

         Socket clientSocket = null;
         try {

             clientSocket = new Socket("10.0.2.2", 3000);
             outputStream = clientSocket.getOutputStream();
             byte[] bitMapBuffer = bitMapString.getBytes();
             outputStream.write(bitMapBuffer);
             outputStream.close();
             inputStream = clientSocket.getInputStream();
             byte[] buffer = new byte[1024];
             int actuallyRead = inputStream.read(buffer);
             inputStream.close();
             if(actuallyRead > 0)
                 result = new String(buffer, 0 , actuallyRead);


         } catch (IOException e) {
             e.printStackTrace();
         }
         finally {
             if(inputStream != null)
             {
                 try {
                     inputStream.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             if(outputStream != null)
             {
                 try {
                     outputStream.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
    return result;
     }

     @Override
     protected void onPostExecute(String s) {
         String[] keyPairs = s.split("=");
         if (keyPairs[0]== "RESULT" && keyPairs[1]=="OK")
         {
             Toast.makeText(MainActivity.this, "Successfully sent to server", Toast.LENGTH_SHORT).show();
         }
     }
 }
}
