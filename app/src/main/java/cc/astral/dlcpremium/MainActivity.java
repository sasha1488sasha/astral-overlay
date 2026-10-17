package cc.astral.dlcpremium;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int BG=Color.rgb(9,9,11), RED=Color.rgb(255,48,56), WHITE=Color.WHITE, MUTED=Color.rgb(160,160,168);
    private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int color,int radius,int stroke){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;
    }
    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);return t;
    }
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);showKey();}
    private void showKey(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(24),dp(36),dp(24),dp(24));root.setBackgroundColor(BG);
        root.addView(text("ASTRAL",34,WHITE,true));root.addView(text("DLC",34,RED,true));
        TextView sub=text("PREMIUM 15.3  •  astral.cc",12,MUTED,false);sub.setPadding(0,dp(8),0,dp(28));root.addView(sub);
        root.addView(text("KEY",13,WHITE,true));
        EditText key=new EditText(this);key.setSingleLine(true);key.setHint("Enter access key");key.setHintTextColor(MUTED);key.setTextColor(WHITE);key.setPadding(dp(12),0,dp(12),0);key.setBackground(shape(Color.rgb(23,23,27),12,Color.DKGRAY));root.addView(key,lp(-1,52));
        Button load=new Button(this);load.setText("LOAD");load.setAllCaps(false);load.setTextColor(WHITE);load.setBackground(shape(RED,12,RED));LinearLayout.LayoutParams p=lp(-1,50);p.setMargins(0,dp(14),0,0);root.addView(load,p);
        TextView hint=text("Demo key: ASTRAL-152",12,MUTED,false);hint.setPadding(0,dp(12),0,0);root.addView(hint);
        load.setOnClickListener(v->{
            if(!"ASTRAL-152".equals(key.getText().toString().trim())){Toast.makeText(this,"Invalid key.",Toast.LENGTH_SHORT).show();return;}
            if(!Settings.canDrawOverlays(this)){
                new AlertDialog.Builder(this).setTitle("Allow overlay")
                    .setMessage("Android requires you to allow ASTRALDLC to display over other apps. Enable the permission, then press LOAD again.")
                    .setNegativeButton("Cancel",null).setPositiveButton("Open Settings",(d,w)->{
                        Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));startActivity(i);
                    }).show();return;
            }
            startService(new Intent(this,OverlayService.class));
            Intent game=getPackageManager().getLaunchIntentForPackage("com.axlebolt.standoff2");
            if(game!=null){try{startActivity(game);}catch(Exception e){Toast.makeText(this,"Could not launch Standoff 2.",Toast.LENGTH_LONG).show();}}
            else Toast.makeText(this,"Overlay started. Standoff 2 was not found; install the official game first.",Toast.LENGTH_LONG).show();
        });
        setContentView(root);
    }
}
