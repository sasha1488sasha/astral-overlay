package cc.astral.dlcpremium;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.widget.EditText;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverlayService extends Service {
    private static final int BG=Color.rgb(9,9,11), PANEL=Color.rgb(20,20,24), RED=Color.rgb(255,48,56), WHITE=Color.WHITE, MUTED=Color.rgb(165,165,172);
    private WindowManager wm; private View watermark; private LinearLayout panel; private WindowManager.LayoutParams markLp,panelLp;
    private boolean opened=false; private String tab="HOME"; private SharedPreferences prefs;
    private float downX,downY,startX,startY; private boolean dragging=false;
    private final String[] tabs={"HOME","AIMBOT","VISUALS","OPTIMIZE","CONFIGS","SETTINGS","ABOUT"};
    private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable shape(int c,int r,int s){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));if(s!=0)d.setStroke(dp(1),s);return d;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private TextView txt(String s,int size,int c,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(c);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private Button btn(String s,boolean active){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(10);b.setTextColor(active?WHITE:MUTED);b.setPadding(dp(7),0,dp(7),0);b.setBackground(shape(active?RED:PANEL,10,active?RED:Color.DKGRAY));return b;}
    @Override public void onCreate(){
        super.onCreate();if(!Settings.canDrawOverlays(this)){stopSelf();return;}
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);prefs=getSharedPreferences("astraldlcpremium",MODE_PRIVATE);
        createWatermark();
    }
    private int windowType(){return android.os.Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    private void createWatermark(){
        LinearLayout chip=row();chip.setPadding(dp(10),dp(5),dp(10),dp(5));chip.setBackground(shape(Color.rgb(22,22,26),22,Color.rgb(75,75,82)));
        TextView dot=txt("●",12,RED,true);chip.addView(dot);TextView name=txt("  Astraldlc  |  astral.cc",12,WHITE,true);chip.addView(name);watermark=chip;
        markLp=new WindowManager.LayoutParams(-2,dp(38),windowType(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        markLp.gravity=Gravity.TOP|Gravity.RIGHT;markLp.x=dp(12);markLp.y=dp(24);
        chip.setOnClickListener(v->{if(opened)removePanel();else showPanel();});
        try{wm.addView(chip,markLp);}catch(Exception e){stopSelf();}
    }
    private void showPanel(){
        if(opened||wm==null)return;opened=true;
        panel=col();panel.setPadding(dp(10),dp(10),dp(10),dp(10));panel.setBackground(shape(Color.rgb(12,12,15),15,RED));panel.setElevation(dp(10));
        LinearLayout title=row();title.addView(txt("ASTRAL",17,WHITE,true));title.addView(txt("DLC",17,RED,true));
        View gap=new View(this);title.addView(gap,new LinearLayout.LayoutParams(0,1,1));
        TextView close=txt("×",24,WHITE,true);close.setGravity(Gravity.CENTER);title.addView(close,lp(30,30));close.setOnClickListener(v->removePanel());panel.addView(title);
        panel.addView(txt("Astraldlc  •  astral.cc",10,MUTED,false));
        ScrollView navScroll=new ScrollView(this);LinearLayout nav=row();
        for(String t:tabs){Button b=btn(t,t.equals(tab));b.setOnClickListener(v->{tab=t;removePanel();showPanel();});LinearLayout.LayoutParams p=lp(-2,36);p.setMargins(0,dp(5),dp(4),dp(5));nav.addView(b,p);}
        navScroll.addView(nav);panel.addView(navScroll,lp(-1,45));
        ScrollView body=new ScrollView(this);LinearLayout items=col();body.addView(items);panel.addView(body,lp(-1,250));renderTab(items);
        panel.addView(txt("DRAG PANEL • ASTRALDLC",9,MUTED,false));
        panelLp=new WindowManager.LayoutParams(-1,dp(355),windowType(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        panelLp.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;panelLp.y=dp(95);
        title.setOnTouchListener((v,e)->{
            switch(e.getAction()){
                case MotionEvent.ACTION_DOWN:downX=e.getRawX();downY=e.getRawY();startX=panelLp.x;startY=panelLp.y;dragging=false;return true;
                case MotionEvent.ACTION_MOVE:
                    float dx=e.getRawX()-downX,dy=e.getRawY()-downY;
                    if(Math.abs(dx)+Math.abs(dy)>dp(6))dragging=true;
                    if(dragging){panelLp.x=(int)(startX+dx);panelLp.y=Math.max(0,(int)(startY+dy));try{wm.updateViewLayout(panel,panelLp);}catch(Exception ignored){}}
                    return true;
                case MotionEvent.ACTION_UP:return true;
            }return true;
        });
        try{wm.addView(panel,panelLp);}catch(Exception e){opened=false;Toast.makeText(this,"Could not display overlay.",Toast.LENGTH_LONG).show();}
    }
    private void removePanel(){if(panel!=null&&wm!=null){try{wm.removeView(panel);}catch(Exception ignored){}panel=null;}opened=false;}
    private void renderTab(LinearLayout c){
        switch(tab){
            case "HOME":card(c,"HOME","ASTRALDLC PREMIUM 15.3\nOverlay controls and local preferences.");break;
            case "AIMBOT":card(c,"AIMBOT","Not implemented. No aiming automation, wallhack, game memory access, injection, or anti-cheat bypass.");break;
            case "VISUALS":
                card(c,"VISUALS","App-side demo preferences only; they do not modify Standoff 2.");
                sw(c,"Potato Graphics","potato",false);sw(c,"Reduced Effects","effects",false);sw(c,"Low Detail","low_detail",false);sw(c,"Visual Preset","visual_preset",false);break;
            case "OPTIMIZE":card(c,"OPTIMIZE","No persistent background loops. These preferences do not claim to improve game FPS.");sw(c,"Reduce UI animations","reduced_animations",true);sw(c,"Compact layout","compact",false);break;
            case "CONFIGS":
                action(c,"SAVE CONFIG",this::saveConfig);action(c,"LOAD CONFIG",this::loadConfig);action(c,"RENAME CONFIG",this::renameConfig);action(c,"DELETE CONFIG",this::deleteConfig);action(c,"RESET SETTINGS",this::resetSettings);
                List<String> ns=configNames();card(c,"SAVED CONFIGS",ns.isEmpty()?"No configs saved":android.text.TextUtils.join("\n",ns));break;
            case "SETTINGS":card(c,"SETTINGS","Settings are stored locally on this device.");sw(c,"Reduced Effects","effects",false);sw(c,"Compact layout","compact",false);action(c,"Reset Settings",this::resetSettings);break;
            case "ABOUT":card(c,"ASTRALDLC","15.3 Overlay Template\nAstral | astral.cc | @straldlc\nNo game injection.");break;
        }
    }
    private void card(LinearLayout c,String h,String d){LinearLayout box=col();box.setPadding(dp(9),dp(8),dp(9),dp(8));box.setBackground(shape(PANEL,10,Color.rgb(44,44,50)));box.addView(txt(h,12,WHITE,true));TextView detail=txt(d,10,MUTED,false);detail.setPadding(0,dp(3),0,0);box.addView(detail);LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,0,0,dp(6));c.addView(box,p);}
    private void action(LinearLayout c,String s,Runnable r){Button b=btn(s,true);b.setOnClickListener(v->r.run());LinearLayout.LayoutParams p=lp(-1,38);p.setMargins(0,0,0,dp(5));c.addView(b,p);}
    private void sw(LinearLayout c,String label,String key,boolean def){LinearLayout r=row();r.setPadding(dp(8),dp(4),dp(4),dp(4));r.setBackground(shape(PANEL,9,Color.DKGRAY));r.addView(txt(label,11,WHITE,true),new LinearLayout.LayoutParams(0,-2,1));android.widget.Switch s=new android.widget.Switch(this);s.setChecked(prefs.getBoolean(key,def));s.setOnCheckedChangeListener((b,v)->prefs.edit().putBoolean(key,v).apply());r.addView(s);LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,0,0,dp(5));c.addView(r,p);}
    private File dir(){File f=new File(getFilesDir(),"configs");if(!f.exists())f.mkdirs();return f;}
    private File cfg(String n){return new File(dir(),n+".json");}
    private String safe(String s){if(s==null)return null;s=s.trim().replaceAll("[^A-Za-z0-9 _-]","").trim();return s.isEmpty()||s.length()>48?null:s;}
    private List<String> configNames(){File[] fs=dir().listFiles((d,n)->n.endsWith(".json"));List<String> out=new ArrayList<>();if(fs!=null)for(File f:fs)out.add(f.getName().replaceFirst("\\.json$",""));Collections.sort(out,String.CASE_INSENSITIVE_ORDER);return out;}
    private JSONObject snapshot()throws Exception{JSONObject o=new JSONObject();for(String k:new String[]{"potato","effects","low_detail","visual_preset","reduced_animations","compact"})o.put(k,prefs.getBoolean(k,k.equals("reduced_animations")));o.put("schema_version",1);return o;}
    private void saveConfig(){EditText e=new EditText(this);e.setHint("Config name");new AlertDialog.Builder(this).setTitle("Save Config").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{String n=safe(e.getText().toString());if(n==null){toast("Invalid name");return;}if(cfg(n).exists()){toast("Name exists");return;}try(FileOutputStream out=new FileOutputStream(cfg(n))){out.write(snapshot().toString(2).getBytes(StandardCharsets.UTF_8));out.getFD().sync();toast("Saved");removePanel();showPanel();}catch(Exception ex){toast("Save failed");}}).show();}
    private void choose(String title,java.util.function.Consumer<String> a){List<String> ns=configNames();if(ns.isEmpty()){toast("No saved configs");return;}String[] ar=ns.toArray(new String[0]);new AlertDialog.Builder(this).setTitle(title).setItems(ar,(d,i)->a.accept(ar[i])).setNegativeButton("Cancel",null).show();}
    private void loadConfig(){choose("Load Config",n->{try(FileInputStream in=new FileInputStream(cfg(n))){byte[] b=new byte[(int)Math.min(cfg(n).length(),1024*1024)];int len=in.read(b);JSONObject o=new JSONObject(len<0?"":new String(b,0,len,StandardCharsets.UTF_8));if(o.length()==0){toast("Empty config");return;}SharedPreferences.Editor ed=prefs.edit();for(String k:new String[]{"potato","effects","low_detail","visual_preset","reduced_animations","compact"})if(o.has(k))ed.putBoolean(k,o.getBoolean(k));ed.apply();toast("Loaded");}catch(Exception e){toast("Invalid config JSON");}});}
    private void renameConfig(){choose("Rename Config",old->{EditText e=new EditText(this);e.setText(old);new AlertDialog.Builder(this).setTitle("New name").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Rename",(d,w)->{String n=safe(e.getText().toString());if(n==null||cfg(n).exists()){toast("Invalid name or name exists");return;}if(cfg(old).renameTo(cfg(n)))toast("Renamed");else toast("Rename failed");}).show();});}
    private void deleteConfig(){choose("Delete Config",n->new AlertDialog.Builder(this).setTitle("Delete "+n+"?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{if(cfg(n).delete())toast("Deleted");else toast("Delete failed");}).show());}
    private void resetSettings(){new AlertDialog.Builder(this).setTitle("Reset settings?").setMessage("Saved config files remain.").setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->{prefs.edit().clear().apply();toast("Settings reset");}).show();}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public int onStartCommand(Intent i,int flags,int id){return START_NOT_STICKY;}
    @Override public void onDestroy(){removePanel();if(wm!=null&&watermark!=null){try{wm.removeView(watermark);}catch(Exception ignored){}}super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
