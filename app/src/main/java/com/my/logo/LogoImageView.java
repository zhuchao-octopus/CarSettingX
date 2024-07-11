package com.my.logo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LogoImageView extends ImageView {
	
	public LogoImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LogoImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);	
	}

	public LogoImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	
	}
	
	 @Override  
     protected void onDraw(Canvas canvas) {  
         super.onDraw(canvas);
          
         Rect rec=canvas.getClipBounds();  
         rec.bottom--;  
         rec.right--;  
         Paint paint=new Paint();  
         paint.setColor(Color.GRAY);  
         paint.setStyle(Paint.Style.STROKE);  
         canvas.drawRect(rec, paint); 
     } 
	
	 public void setImageDrawable(Drawable drawable) {
//		 Bitmap bitmap = Bitmap.createBitmap( drawable.getMinimumWidth(), drawable.getMinimumHeight(), Config.ARGB_8888 );  
//		 Canvas canvas = new Canvas(bitmap); 
//		 canvas.drawColor(Color.BLACK);  
//		 Drawable background = new BitmapDrawable(bitmap);  
//		 setBackgroundDrawable(background); 
		 super.setImageDrawable(drawable);		    
	       
	       
	}
	
}
