package com.example.tablette11;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity   implements SurfaceHolder.Callback{

	public Button monBouton;
	public Button mBoutonoui;
	public Button mBoutonnon;
	
	public SurfaceView maSurfaceView;
	private Camera camera;
	private Boolean isPreview;
	private FileOutputStream stream;
	private ImageView maImageView;
	private TextView txtView;
	public String imageTitle;
	public String imageFileName;
	public Uri taken;
	public Bitmap myBitmap;
	private RelativeLayout  mRelativeLayout;
	public String ba1;
	public static String URL = "http://192.168.119.212/server_2.php";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        isPreview = false;
        txtView = (TextView) findViewById(R.id.textView1);
        txtView.setText("...");
        
        maSurfaceView = (SurfaceView) findViewById(R.id.surfaceView1);
        maImageView =(ImageView) findViewById(R.id.imageView1);
        mRelativeLayout =(RelativeLayout) findViewById(R.id.RelativeLayout1);
        mRelativeLayout.setVisibility(View.INVISIBLE);
        monBouton = (Button) findViewById(R.id.button1);
        mBoutonoui = (Button) findViewById(R.id.buttonoui);
        mBoutonnon = (Button) findViewById(R.id.buttonno);
        
        mBoutonoui.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// envoyer image
				// proposer interface d'envoie
				//maSurfaceView.setVisibility(View.VISIBLE);
				mRelativeLayout.setVisibility(View.INVISIBLE);
				monBouton.setVisibility(View.VISIBLE);
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
		        byte[] ba = bao.toByteArray();
		        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
		        Log.e("base64", "-----" + ba1);
		        
		        new uploadToServer().execute();
			}
				
		});  
        
        mBoutonnon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// envoyer image
				
				//mRelativeLayout.setVisibility(View.INVISIBLE);
				maSurfaceView.setVisibility(View.VISIBLE);
				monBouton.setVisibility(View.VISIBLE);
			
			}
				
		}); 
			        
				
        
        
        monBouton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				txtView.setText("click");
				if (camera != null) {
					SavePicture();
					txtView.setText("camera");
					
				}
				else{
					 txtView.setText("no camera");
				}
							
			}
		});      
			        
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			  .add(R.id.container, new PlaceholderFragment())
			   .commit();
		}
			
			        // M�thode d'initialisation de la cam�ra
		InitializeCamera();
		}
			
			 // Callback pour la prise de photo
		Camera.PictureCallback pictureCallback = new Camera.PictureCallback(){

	        public void onPictureTaken(byte[] data, Camera camera) {
	            if (data != null) {
	                // Enregistrement de votre image
	                try {
	                    if (stream != null) {
	                        stream.write(data);
	                        stream.flush();
	                        stream.close();
	                    }
	                } catch (Exception e) {
	                    // TODO: handle exception
	                    txtView.setText("error");
	                }
	
	                txtView.setText("Picture saved");
	                txtView.setText(imageFileName.toString());
	                
	                File imgFile = new  File(imageFileName);

	                if(imgFile.exists()){
	                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());	                    
	                    maImageView.setImageBitmap(myBitmap);
	                }/*else{
	                	txtView.setText(taken.getPath() + " not found !");
	                }*/
	                maSurfaceView.setVisibility(View.INVISIBLE);
					mRelativeLayout.setVisibility(View.VISIBLE);
					monBouton.setVisibility(View.INVISIBLE);
	                // Nous red�marrons la pr�visualisation
	                //camera.startPreview();
	            }
	        }
		};
    
	private void SavePicture() {
		try {
			SimpleDateFormat timeStampFormat = new SimpleDateFormat(
                "yyyy-MM-dd-HH.mm.ss");
			imageTitle = "photo_" + timeStampFormat.format(new Date())
                + ".jpg";
            txtView.setText(imageTitle);

			// Metadata pour la photo
			ContentValues values = new ContentValues();
			values.put(Media.TITLE, imageTitle);
			values.put(Media.DISPLAY_NAME, imageTitle);
			values.put(Media.DESCRIPTION, "Image prise par FormationCamera");
			values.put(Media.DATE_TAKEN, new Date().getTime());
			values.put(Media.MIME_TYPE, "image/jpeg");

			// Support de stockage
			taken = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI,
                values);

			// Ouverture du flux pour la sauvegarde
			stream = (FileOutputStream) getContentResolver().openOutputStream(
                taken);
			
			Cursor c = getContentResolver().query(taken, null, null, null, null);

	        if (c != null && c.moveToFirst()) {

	            int id = c.getColumnIndex(Images.Media.DATA);
	            if (id != -1) {
	                this.imageFileName = c.getString(id);
	            }
	        }

			camera.takePicture(null, pictureCallback, pictureCallback);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}


    private void InitializeCamera() {
    	// Nous attachons nos retours du holder � notre activit�
    	maSurfaceView.getHolder().addCallback(this);
    	// Nous sp�cifiions le type du holder en mode SURFACE_TYPE_PUSH_BUFFERS
    	maSurfaceView.getHolder().setType(
    	SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);		
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    // Nous prenons le contr�le de la camera
	    if (camera == null)
	        camera = Camera.open();		
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	    // Si le mode preview est lanc� alors nous le stoppons
	    if (isPreview) {
	        camera.stopPreview();
	    }
	    // Nous r�cup�rons les param�tres de la cam�ra
	    Camera.Parameters parameters = camera.getParameters();

	    // Nous changeons la taille
	    parameters.setPreviewSize(width, height);

	    // Nous appliquons nos nouveaux param�tres
	    //camera.setParameters(parameters);

	    try {
	        // Nous attachons notre pr�visualisation de la cam�ra au holder de la
	        // surface
	        camera.setPreviewDisplay(maSurfaceView.getHolder());
	    } catch (IOException e) {
	    }

	    // Nous lan�ons la preview
	    camera.startPreview();

	    isPreview = true;
		
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    // Nous arr�tons la camera et nous rendons la main
	    if (camera != null) {
	        camera.stopPreview();
	        isPreview = false;
	        camera.release();	
	    }
	}
	
	// Retour sur l'application
	@Override
	public void onResume() {
	    super.onResume();
	    camera = Camera.open();
	}

	// Mise en pause de l'application
	@Override
	public void onPause() {
	    super.onPause();

	    if (camera != null) {
	        camera.release();
	        camera = null;
	    }
	}
	public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(MainActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("base64", ba1));
            nameValuePairs.add(new BasicNameValuePair("ImageName", System.currentTimeMillis() + ".jpg"));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String st = EntityUtils.toString(response.getEntity());
                Log.v("log_tag", "In the try Loop" + st);

            } catch (Exception e) {
                Log.v("log_tag", "Error in http connection " + e.toString());
            }
            return "Success";

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
        }
    }


}
