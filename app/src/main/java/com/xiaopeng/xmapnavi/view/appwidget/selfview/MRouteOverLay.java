//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xiaopeng.xmapnavi.view.appwidget.selfview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.amap.api.col.el;
import com.amap.api.col.em;
import com.amap.api.col.hk;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.NavigateArrow;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviException;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapTrafficStatus;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.RouteOverlayOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import com.xiaopeng.xmapnavi.R;
public class MRouteOverLay {
    private Bitmap startBitmap;
    private Bitmap endBitmap;
    private Bitmap wayBitmap;
    private BitmapDescriptor startBitmapDescriptor;
    private BitmapDescriptor endBitmapDescriptor;
    private BitmapDescriptor wayPointBitmapDescriptor;
    private Marker startMarker;
    private List<Marker> wayMarkers;
    private Marker endMarker;
    private BitmapDescriptor arrowOnRoute = null;
    private BitmapDescriptor normalRoute = null;
    private BitmapDescriptor unknownTraffic = null;
    private BitmapDescriptor smoothTraffic = null;
    private BitmapDescriptor slowTraffic = null;
    private BitmapDescriptor jamTraffic = null;
    private BitmapDescriptor veryJamTraffic = null;
    private List<Polyline> mTrafficColorfulPolylines = new ArrayList();
    private RouteOverlayOptions mRouteOverlayOptions = null;
    private float mWidth = 40.0F;
    private AMapNaviPath mAMapNaviPath = null;
    private Polyline mDefaultPolyline;
    private AMap aMap;
    private Context mContext;
    private List<LatLng> mLatLngsOfPath;
    private Polyline guideLink = null;
    private List<Circle> gpsCircles = null;
    private boolean emulateGPSLocationVisibility = true;
    private NavigateArrow naviArrow = null;
    private boolean isTrafficLine = true;
    private List<Polyline> mCustomPolylines = new ArrayList();

    public MRouteOverLay(AMap var1, AMapNaviPath var2, Context var3) {
        this.mContext = var3;
        this.init(var1, var2);
    }

    public boolean isPolyLineInIt(Polyline polyline){
        if (guideLink!=null && polyline.equals(guideLink))return true;
        if (mDefaultPolyline!=null && polyline.equals(mDefaultPolyline))return true;
        for (Polyline pp:mCustomPolylines){
            if (pp.equals(polyline))return true;
        }
        for (Polyline p2:mTrafficColorfulPolylines){
            if (p2.equals(polyline))return true;
        }
        return false;
    }


    public float getWidth() {
        return this.mWidth;
    }

    public void setWidth(float var1) throws AMapNaviException {
        if(var1 <= 0.0F) {
            throw new AMapNaviException("非法参数-宽度必须>0");
        } else {
            this.mWidth = var1;
        }
    }

    public RouteOverlayOptions getRouteOverlayOptions() {
        return this.mRouteOverlayOptions;
    }

    public void setRouteOverlayOptions(RouteOverlayOptions var1) {
        this.mRouteOverlayOptions = var1;
        if(var1 != null && var1.getNormalRoute() != null) {
            this.normalRoute = BitmapDescriptorFactory.fromBitmap(var1.getNormalRoute());
        }

        if(var1 != null && var1.getArrowOnTrafficRoute() != null) {
            this.arrowOnRoute = BitmapDescriptorFactory.fromBitmap(var1.getArrowOnTrafficRoute());
        }

        if(var1 != null && var1.getUnknownTraffic() != null) {
            this.unknownTraffic = BitmapDescriptorFactory.fromBitmap(var1.getUnknownTraffic());
        }

        if(var1 != null && var1.getSmoothTraffic() != null) {
            this.smoothTraffic = BitmapDescriptorFactory.fromBitmap(var1.getSmoothTraffic());
        }

        if(var1 != null && var1.getSlowTraffic() != null) {
            this.slowTraffic = BitmapDescriptorFactory.fromBitmap(var1.getSlowTraffic());
        }

        if(var1 != null && var1.getJamTraffic() != null) {
            this.jamTraffic = BitmapDescriptorFactory.fromBitmap(var1.getJamTraffic());
        }

        if(var1 != null && var1.getVeryJamTraffic() != null) {
            this.veryJamTraffic = BitmapDescriptorFactory.fromBitmap(var1.getVeryJamTraffic());
        }

        if(var1 != null && var1.getLineWidth() > 0.0F) {
            this.mWidth = var1.getLineWidth();
        }

    }

    public AMapNaviPath getAMapNaviPath() {
        return this.mAMapNaviPath;
    }

    public void setAMapNaviPath(AMapNaviPath var1) {
        this.mAMapNaviPath = var1;
    }

    /** @deprecated */
    @Deprecated
    public void setRouteInfo(AMapNaviPath var1) {
        this.mAMapNaviPath = var1;
    }

    private void init(AMap var1, AMapNaviPath var2) {
        try {
            this.aMap = var1;
            this.mAMapNaviPath = var2;
            this.normalRoute = BitmapDescriptorFactory.fromResource(R.drawable.custtexture_green_new);
        } catch (Throwable var4) {
            el.a(var4);
            hk.b(var4, "RouteOverLay", "init(AMap amap, AMapNaviPath aMapNaviPath)");
        }


        this.arrowOnRoute = BitmapDescriptorFactory.fromAsset("custtexture_aolr.png");
        this.smoothTraffic = BitmapDescriptorFactory.fromResource(R.drawable.custtexture_green_new);
        this.unknownTraffic = BitmapDescriptorFactory.fromResource(R.drawable.custtexture_green_new);
        this.slowTraffic = BitmapDescriptorFactory.fromResource(R.drawable.mcusttexture_green_2);
        this.jamTraffic = BitmapDescriptorFactory.fromResource(R.drawable.mcusttexture_green_3);
        this.veryJamTraffic = BitmapDescriptorFactory.fromResource(R.drawable.mcusttexture_green_4);
    }

    public void addToMap() {
        try {
            if(this.aMap == null) {
                return;
            }

            if(this.mDefaultPolyline != null) {
                this.mDefaultPolyline.remove();
                this.mDefaultPolyline = null;
            }

            if(this.mWidth == 0.0F || this.mAMapNaviPath == null) {
                return;
            }

            if(this.naviArrow != null) {
                this.naviArrow.setVisible(false);
            }

            List var1 = null;
            var1 = this.mAMapNaviPath.getCoordList();
            if(var1 == null) {
                return;
            }

            int var2 = var1.size();
            this.mLatLngsOfPath = new ArrayList(var2);
            Iterator var3 = var1.iterator();

            while(var3.hasNext()) {
                NaviLatLng var4 = (NaviLatLng)var3.next();
                LatLng var5 = new LatLng(var4.getLatitude(), var4.getLongitude(), false);
                this.mLatLngsOfPath.add(var5);
            }

            if(this.mLatLngsOfPath.size() == 0) {
                return;
            }

            this.clearTrafficLineAndInvisibleOriginalLine();
            this.mDefaultPolyline = this.aMap.addPolyline((new PolylineOptions()).addAll(this.mLatLngsOfPath).setCustomTexture(this.normalRoute).width(this.mWidth));
            this.mDefaultPolyline.setVisible(true);
            LatLng var12 = null;
            LatLng var13 = null;
            List var14 = null;
            if(this.mAMapNaviPath.getStartPoint() != null && this.mAMapNaviPath.getEndPoint() != null) {
                var12 = new LatLng(this.mAMapNaviPath.getStartPoint().getLatitude(), this.mAMapNaviPath.getStartPoint().getLongitude());
                var13 = new LatLng(this.mAMapNaviPath.getEndPoint().getLatitude(), this.mAMapNaviPath.getEndPoint().getLongitude());
                var14 = this.mAMapNaviPath.getWayPoint();
            }

            if(this.startMarker != null) {
                this.startMarker.remove();
                this.startMarker = null;
            }

            if(this.endMarker != null) {
                this.endMarker.remove();
                this.endMarker = null;
            }

            int var6;
            if(this.wayMarkers != null && this.wayMarkers.size() > 0) {
                for(var6 = 0; var6 < this.wayMarkers.size(); ++var6) {
                    Marker var7 = (Marker)this.wayMarkers.get(var6);
                    if(var7 != null) {
                        var7.remove();
                        var7 = null;
                    }
                }
            }

            if(this.startBitmap == null) {
                this.startMarker = this.aMap.addMarker((new MarkerOptions()).position(var12).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(em.a(), 1191313528))));
            } else if(this.startBitmapDescriptor != null) {
                this.startMarker = this.aMap.addMarker((new MarkerOptions()).position(var12).icon(this.startBitmapDescriptor));
            }

            if(var14 != null && var14.size() > 0) {
                var6 = var14.size();
                if(this.wayMarkers == null) {
                    this.wayMarkers = new ArrayList(var6);
                }

                Marker var10;
                for(Iterator var15 = var14.iterator(); var15.hasNext(); this.wayMarkers.add(var10)) {
                    NaviLatLng var8 = (NaviLatLng)var15.next();
                    LatLng var9 = new LatLng(var8.getLatitude(), var8.getLongitude());
                    var10 = null;
                    if(this.wayBitmap == null) {
                        var10 = this.aMap.addMarker((new MarkerOptions()).position(var9).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(em.a(), 1191313535))));
                    } else if(this.wayPointBitmapDescriptor != null) {
                        var10 = this.aMap.addMarker((new MarkerOptions()).position(var9).icon(this.wayPointBitmapDescriptor));
                    }
                }
            }

            if(this.endBitmap == null) {
                this.endMarker = this.aMap.addMarker((new MarkerOptions()).position(var13).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(em.a(), 1191313417))));
            } else if(this.endBitmapDescriptor != null) {
                this.endMarker = this.aMap.addMarker((new MarkerOptions()).position(var13).icon(this.endBitmapDescriptor));
            }

            if(this.isTrafficLine) {
                this.setTrafficLine(Boolean.valueOf(this.isTrafficLine));
            }
        } catch (Throwable var11) {
            el.a(var11);
            hk.b(var11, "RouteOverLay", "addToMap()");
        }

    }

    public void drawGuideLink(LatLng var1, LatLng var2, boolean var3) {
        if(var3) {
            ArrayList var4 = new ArrayList(2);
            var4.add(var1);
            var4.add(var2);
            if(this.guideLink == null) {
                this.guideLink = this.aMap.addPolyline((new PolylineOptions()).addAll(var4).width(this.mWidth / 3.0F).setDottedLine(true));
            } else {
                this.guideLink.setPoints(var4);
            }

            this.guideLink.setVisible(true);
        } else if(this.guideLink != null) {
            this.guideLink.setVisible(false);
        }

    }

    public void drawEmulateGPSLocation(Vector<String> var1) {
        try {
            Iterator var2;
            if(this.gpsCircles == null) {
                this.gpsCircles = new ArrayList(var1.size());
            } else {
                var2 = this.gpsCircles.iterator();

                while(true) {
                    if(!var2.hasNext()) {
                        this.gpsCircles.clear();
                        break;
                    }

                    Circle var3 = (Circle)var2.next();
                    var3.remove();
                }
            }

            var2 = var1.iterator();

            while(var2.hasNext()) {
                String var8 = (String)var2.next();
                String[] var4 = var8.split(",");
                if(var4 != null && var4.length >= 11) {
                    LatLng var5 = new LatLng(Double.parseDouble(var4[0]), Double.parseDouble(var4[1]));
                    Circle var6 = this.aMap.addCircle((new CircleOptions()).center(var5).radius(1.5D).strokeWidth(0.0F).fillColor(-65536));
                    this.gpsCircles.add(var6);
                }
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            hk.b(var7, "RouteOverLay", "drawEmulateGPSLocation(Vector<String> gpsData)");
        }

    }

    public void setEmulateGPSLocationVisible() {
        if(this.gpsCircles != null) {
            this.emulateGPSLocationVisibility = !this.emulateGPSLocationVisibility;
            Iterator var1 = this.gpsCircles.iterator();

            while(var1.hasNext()) {
                Circle var2 = (Circle)var1.next();
                var2.setVisible(this.emulateGPSLocationVisibility);
            }
        }

    }

    public void setStartPointBitmap(Bitmap var1) {
        this.startBitmap = var1;
        if(this.startBitmap != null) {
            this.startBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(this.startBitmap);
        }

    }

    public void setWayPointBitmap(Bitmap var1) {
        this.wayBitmap = var1;
        if(this.wayBitmap != null) {
            this.wayPointBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(this.wayBitmap);
        }

    }

    public void setEndPointBitmap(Bitmap var1) {
        this.endBitmap = var1;
        if(this.endBitmap != null) {
            this.endBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(this.endBitmap);
        }

    }

    public void removeFromMap() {
        try {
            if(this.mDefaultPolyline != null) {
                this.mDefaultPolyline.setVisible(false);
            }

            if(this.startMarker != null) {
                this.startMarker.setVisible(false);
            }

            Iterator var1;
            if(this.wayMarkers != null) {
                var1 = this.wayMarkers.iterator();

                while(var1.hasNext()) {
                    Marker var2 = (Marker)var1.next();
                    var2.setVisible(false);
                }
            }

            if(this.endMarker != null) {
                this.endMarker.setVisible(false);
            }

            if(this.naviArrow != null) {
                this.naviArrow.remove();
            }

            if(this.guideLink != null) {
                this.guideLink.setVisible(false);
            }

            if(this.gpsCircles != null) {
                var1 = this.gpsCircles.iterator();

                while(var1.hasNext()) {
                    Circle var4 = (Circle)var1.next();
                    var4.setVisible(false);
                }
            }

            this.clearTrafficLineAndInvisibleOriginalLine();
        } catch (Throwable var3) {
            el.a(var3);
            hk.b(var3, "RouteOverLay", "removeFromMap()");
        }

    }

    private void clearTrafficLineAndInvisibleOriginalLine() {
        int var1;
        if(this.mTrafficColorfulPolylines.size() > 0) {
            for(var1 = 0; var1 < this.mTrafficColorfulPolylines.size(); ++var1) {
                if(this.mTrafficColorfulPolylines.get(var1) != null) {
                    ((Polyline)this.mTrafficColorfulPolylines.get(var1)).remove();
                }
            }
        }

        this.mTrafficColorfulPolylines.clear();
        if(this.mDefaultPolyline != null) {
            this.mDefaultPolyline.setVisible(false);
        }

        if(this.mCustomPolylines.size() > 0) {
            for(var1 = 0; var1 < this.mCustomPolylines.size(); ++var1) {
                if(this.mCustomPolylines.get(var1) != null) {
                    ((Polyline)this.mCustomPolylines.get(var1)).setVisible(false);
                }
            }
        }

    }

    private void colorWayUpdate(List<AMapTrafficStatus> var1) {
        if(this.aMap != null) {
            if(this.mLatLngsOfPath != null && this.mLatLngsOfPath.size() > 0) {
                if(var1 != null && var1.size() > 0) {
                    this.clearTrafficLineAndInvisibleOriginalLine();
                    int var2 = 0;
                    LatLng var3 = (LatLng)this.mLatLngsOfPath.get(0);
                    LatLng var4 = null;
                    double var5 = 0.0D;
                    ArrayList var8 = new ArrayList();
                    Polyline var9 = null;

                    for(int var10 = 0; var10 < this.mLatLngsOfPath.size() && var2 < var1.size(); ++var10) {
                        AMapTrafficStatus var7 = (AMapTrafficStatus)var1.get(var2);
                        var4 = (LatLng)this.mLatLngsOfPath.get(var10);
                        NaviLatLng var11 = new NaviLatLng(var3.latitude, var3.longitude);
                        NaviLatLng var12 = new NaviLatLng(var4.latitude, var4.longitude);
                        double var13 = (double)el.a(var11, var12);
                        var5 += var13;
                        if(var5 > (double)(var7.getLength() + 1)) {
                            double var15 = var13 - (var5 - (double)var7.getLength());
                            NaviLatLng var17 = el.a(var11, var12, var15);
                            LatLng var18 = new LatLng(var17.getLatitude(), var17.getLongitude());
                            var8.add(var18);
                            var3 = var18;
                            --var10;
                        } else {
                            var8.add(var4);
                            var3 = var4;
                        }

                        if(var5 >= (double)var7.getLength() || var10 == this.mLatLngsOfPath.size() - 1) {
                            if(var2 == var1.size() - 1 && var10 < this.mLatLngsOfPath.size() - 1) {
                                ++var10;

                                while(var10 < this.mLatLngsOfPath.size()) {
                                    LatLng var19 = (LatLng)this.mLatLngsOfPath.get(var10);
                                    var8.add(var19);
                                    ++var10;
                                }
                            }

                            ++var2;
                            switch(var7.getStatus()) {
                                case 0:
                                    var9 = this.aMap.addPolyline((new PolylineOptions()).addAll(var8).width(this.mWidth).setCustomTexture(this.unknownTraffic));
                                    break;
                                case 1:
                                    var9 = this.aMap.addPolyline((new PolylineOptions()).addAll(var8).width(this.mWidth).setCustomTexture(this.smoothTraffic));
                                    break;
                                case 2:
                                    var9 = this.aMap.addPolyline((new PolylineOptions()).addAll(var8).width(this.mWidth).setCustomTexture(this.slowTraffic));
                                    break;
                                case 3:
                                    var9 = this.aMap.addPolyline((new PolylineOptions()).addAll(var8).width(this.mWidth).setCustomTexture(this.jamTraffic));
                                    break;
                                case 4:
                                    var9 = this.aMap.addPolyline((new PolylineOptions()).addAll(var8).width(this.mWidth).setCustomTexture(this.veryJamTraffic));
                            }

                            this.mTrafficColorfulPolylines.add(var9);
                            var8.clear();
                            var8.add(var3);
                            var5 = 0.0D;
                        }
                    }

                    var9 = this.aMap.addPolyline((new PolylineOptions()).addAll(this.mLatLngsOfPath).width(this.mWidth).setCustomTexture(this.arrowOnRoute));
                    this.mTrafficColorfulPolylines.add(var9);
                }
            }
        }
    }

    public void zoomToSpan() {
        this.zoomToSpan(100);
    }

    public void zoomToSpan(int var1) {
        try {
            if(this.mAMapNaviPath == null) {
                return;
            }

            CameraUpdate var2 = CameraUpdateFactory.newLatLngBounds(this.mAMapNaviPath.getBoundsForPath(), var1);
            this.aMap.animateCamera(var2, 1000L, (CancelableCallback)null);
        } catch (Throwable var3) {
            el.a(var3);
            hk.b(var3, "RouteOverLay", "zoomToSpan()");
        }

    }

    public void destroy() {
        try {
            if(this.mDefaultPolyline != null) {
                this.mDefaultPolyline.remove();
            }

            this.mAMapNaviPath = null;
            if(this.arrowOnRoute != null) {
                this.arrowOnRoute.recycle();
            }

            if(this.smoothTraffic != null) {
                this.smoothTraffic.recycle();
            }

            if(this.unknownTraffic != null) {
                this.unknownTraffic.recycle();
            }

            if(this.slowTraffic != null) {
                this.slowTraffic.recycle();
            }

            if(this.jamTraffic != null) {
                this.jamTraffic.recycle();
            }

            if(this.veryJamTraffic != null) {
                this.veryJamTraffic.recycle();
            }

            if(this.startBitmap != null) {
                this.startBitmap.recycle();
            }

            if(this.endBitmap != null) {
                this.endBitmap.recycle();
            }

            if(this.wayBitmap != null) {
                this.wayBitmap.recycle();
            }
        } catch (Throwable var2) {
            el.a(var2);
            hk.b(var2, "RouteOverLay", "destroy()");
        }

    }

    public void drawArrow(List<NaviLatLng> var1) {
        try {
            if(var1 == null) {
                this.naviArrow.setVisible(false);
                return;
            }

            int var2 = var1.size();
            ArrayList var3 = new ArrayList(var2);
            Iterator var4 = var1.iterator();

            while(var4.hasNext()) {
                NaviLatLng var5 = (NaviLatLng)var4.next();
                LatLng var6 = new LatLng(var5.getLatitude(), var5.getLongitude(), false);
                var3.add(var6);
            }

            if(this.naviArrow == null) {
                this.naviArrow = this.aMap.addNavigateArrow((new NavigateArrowOptions()).addAll(var3).width(this.mWidth * 0.6F));
            } else {
                this.naviArrow.setPoints(var3);
            }

            this.naviArrow.setZIndex(1.0F);
            this.naviArrow.setVisible(true);
        } catch (Throwable var7) {
            var7.printStackTrace();
            hk.b(var7, "RouteOverLay", "drawArrow(List<NaviLatLng> list) ");
        }

    }

    public List<NaviLatLng> getArrowPoints(int var1) {
        if(this.mAMapNaviPath == null) {
            return null;
        } else {
            try {
                if(var1 >= this.mAMapNaviPath.getStepsCount()) {
                    return null;
                }

                List var2 = this.mAMapNaviPath.getCoordList();
                int var3 = var2.size();
                List var4 = this.mAMapNaviPath.getSteps();
                AMapNaviStep var5 = (AMapNaviStep)var4.get(var1);
                int var6 = var5.getEndIndex();
                NaviLatLng var7 = (NaviLatLng)var2.get(var6);
                Vector var8 = new Vector();
                NaviLatLng var9 = var7;
                int var10 = 0;
                byte var11 = 50;

                int var12;
                NaviLatLng var13;
                int var14;
                NaviLatLng var15;
                for(var12 = var6 - 1; var12 >= 0; --var12) {
                    var13 = (NaviLatLng)var2.get(var12);
                    var14 = el.a(var9, var13);
                    var10 += var14;
                    if(var10 >= var11) {
                        var15 = el.a(var9, var13, (double)(var11 + var14 - var10));
                        var8.add(var15);
                        break;
                    }

                    var9 = var13;
                    var8.add(var13);
                }

                Collections.reverse(var8);
                var8.add(var7);
                var10 = 0;
                var9 = var7;

                for(var12 = var6 + 1; var12 < var3; ++var12) {
                    var13 = (NaviLatLng)var2.get(var12);
                    var14 = el.a(var9, var13);
                    var10 += var14;
                    if(var10 >= var11) {
                        var15 = el.a(var9, var13, (double)(var11 + var14 - var10));
                        var8.add(var15);
                        break;
                    }

                    var9 = var13;
                    var8.add(var13);
                }

                if(var8.size() > 2) {
                    return var8;
                }
            } catch (Exception var16) {
                var16.printStackTrace();
                hk.b(var16, "RouteOverLay", "getArrowPoints(int roadIndex)");
            }

            return null;
        }
    }

    public boolean isTrafficLine() {
        return this.isTrafficLine;
    }

    public void setTrafficLine(Boolean var1) {
        try {
            if(this.mContext == null) {
                return;
            }

            this.isTrafficLine = var1.booleanValue();
            List var2 = null;
            this.clearTrafficLineAndInvisibleOriginalLine();
            if(this.isTrafficLine) {
                if(this.mAMapNaviPath != null) {
                    var2 = AMapNavi.getInstance(this.mContext).getTrafficStatuses(0, this.mAMapNaviPath.getAllLength());
                }

                if(var2 == null) {
                    this.NoTrafficStatusDisplay();
                } else {
                    this.colorWayUpdate(var2);
                }
            } else {
                this.NoTrafficStatusDisplay();
            }
        } catch (Throwable var3) {
            var3.printStackTrace();
            hk.b(var3, "RouteOverLay", "setTrafficLine(Boolean enabled)");
        }

    }

    private void NoTrafficStatusDisplay() {
        if(this.mDefaultPolyline != null) {
            this.mDefaultPolyline.setVisible(true);
        }

        if(this.mCustomPolylines.size() > 0) {
            for(int var1 = 0; var1 < this.mCustomPolylines.size(); ++var1) {
                if(this.mCustomPolylines.get(var1) != null) {
                    ((Polyline)this.mCustomPolylines.get(var1)).setVisible(true);
                }
            }
        }

    }

    private void addToMap(int[] var1, int[] var2, BitmapDescriptor[] var3) {
        try {
            if(this.aMap == null) {
                return;
            }

            if(this.mDefaultPolyline != null) {
                this.mDefaultPolyline.remove();
                this.mDefaultPolyline = null;
            }

            if(this.mWidth == 0.0F || this.mAMapNaviPath == null || this.normalRoute == null) {
                return;
            }

            if(this.naviArrow != null) {
                this.naviArrow.setVisible(false);
            }

            List var4 = this.mAMapNaviPath.getCoordList();
            if(var4 == null) {
                return;
            }

            this.clearTrafficLineAndInvisibleOriginalLine();
            int var5 = var4.size();
            this.mLatLngsOfPath = new ArrayList(var5);
            ArrayList var6 = new ArrayList();
            int var7 = 0;
            boolean var9 = false;
            int var19;
            if(var1 == null) {
                var19 = var3.length;
            } else {
                var19 = var1.length;
            }

            Polyline var8;
            for(int var10 = 0; var10 < var19; ++var10) {
                if(var2 == null || var10 >= var2.length || var2[var10] > 0) {
                    var6.clear();

                    while(var7 < var4.size()) {
                        NaviLatLng var11 = (NaviLatLng)var4.get(var7);
                        LatLng var12 = new LatLng(var11.getLatitude(), var11.getLongitude(), false);
                        this.mLatLngsOfPath.add(var12);
                        var6.add(var12);
                        if(var2 != null && var10 < var2.length && var7 == var2[var10]) {
                            break;
                        }

                        ++var7;
                    }

                    if(var3 != null && var3.length != 0) {
                        var8 = this.aMap.addPolyline((new PolylineOptions()).addAll(var6).setCustomTexture(var3[var10]).width(this.mWidth));
                    } else {
                        var8 = this.aMap.addPolyline((new PolylineOptions()).addAll(var6).color(var1[var10]).width(this.mWidth));
                    }

                    var8.setVisible(true);
                    this.mCustomPolylines.add(var8);
                }
            }

            var8 = this.aMap.addPolyline((new PolylineOptions()).addAll(this.mLatLngsOfPath).width(this.mWidth).setCustomTexture(this.arrowOnRoute));
            this.mCustomPolylines.add(var8);
            LatLng var20 = null;
            LatLng var21 = null;
            List var22 = null;
            if(this.mAMapNaviPath.getStartPoint() != null && this.mAMapNaviPath.getEndPoint() != null) {
                var20 = new LatLng(this.mAMapNaviPath.getStartPoint().getLatitude(), this.mAMapNaviPath.getStartPoint().getLongitude());
                var21 = new LatLng(this.mAMapNaviPath.getEndPoint().getLatitude(), this.mAMapNaviPath.getEndPoint().getLongitude());
                var22 = this.mAMapNaviPath.getWayPoint();
            }

            if(this.startMarker != null) {
                this.startMarker.remove();
                this.startMarker = null;
            }

            if(this.endMarker != null) {
                this.endMarker.remove();
                this.endMarker = null;
            }

            int var13;
            if(this.wayMarkers != null && this.wayMarkers.size() > 0) {
                for(var13 = 0; var13 < this.wayMarkers.size(); ++var13) {
                    Marker var14 = (Marker)this.wayMarkers.get(var13);
                    if(var14 != null) {
                        var14.remove();
                        var14 = null;
                    }
                }
            }

            if(this.startBitmap == null) {
                this.startMarker = this.aMap.addMarker((new MarkerOptions()).position(var20).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(em.a(), 1191313528))));
            } else if(this.startBitmapDescriptor != null) {
                this.startMarker = this.aMap.addMarker((new MarkerOptions()).position(var20).icon(this.startBitmapDescriptor));
            }

            if(var22 != null && var22.size() > 0) {
                var13 = var22.size();
                if(this.wayMarkers == null) {
                    this.wayMarkers = new ArrayList(var13);
                }

                Marker var17;
                for(Iterator var23 = var22.iterator(); var23.hasNext(); this.wayMarkers.add(var17)) {
                    NaviLatLng var15 = (NaviLatLng)var23.next();
                    LatLng var16 = new LatLng(var15.getLatitude(), var15.getLongitude());
                    var17 = null;
                    if(this.wayBitmap == null) {
                        var17 = this.aMap.addMarker((new MarkerOptions()).position(var16).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(em.a(), 1191313535))));
                    } else if(this.wayPointBitmapDescriptor != null) {
                        var17 = this.aMap.addMarker((new MarkerOptions()).position(var16).icon(this.wayPointBitmapDescriptor));
                    }
                }
            }

            if(this.endBitmap == null) {
                this.endMarker = this.aMap.addMarker((new MarkerOptions()).position(var21).icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(em.a(), 1191313417))));
            } else if(this.endBitmapDescriptor != null) {
                this.endMarker = this.aMap.addMarker((new MarkerOptions()).position(var21).icon(this.endBitmapDescriptor));
            }

            if(this.isTrafficLine) {
                this.setTrafficLine(Boolean.valueOf(this.isTrafficLine));
            }
        } catch (Throwable var18) {
            el.a(var18);
            hk.b(var18, "RouteOverLay", "addToMap(int[] color, int[] index, BitmapDescriptor[] resourceArray)");
        }

    }

    public void addToMap(int[] var1, int[] var2) {
        if(var1 != null && var1.length != 0) {
            this.addToMap(var1, var2, (BitmapDescriptor[])null);
        }
    }

    public void addToMap(BitmapDescriptor[] var1, int[] var2) {
        if(var1 != null && var1.length != 0) {
            this.addToMap((int[])null, var2, var1);
        }
    }

    public void setTransparency(float var1) {
        if(var1 < 0.0F) {
            var1 = 0.0F;
        } else if(var1 > 1.0F) {
            var1 = 1.0F;
        }

        if(this.mDefaultPolyline != null) {
            this.mDefaultPolyline.setTransparency(var1);
        }

        Iterator var2 = this.mTrafficColorfulPolylines.iterator();

        while(var2.hasNext()) {
            Polyline var3 = (Polyline)var2.next();
            var3.setTransparency(var1);
        }

    }

    public void setZindex(int var1) {
        try {
            if(this.mTrafficColorfulPolylines != null) {
                for(int var2 = 0; var2 < this.mTrafficColorfulPolylines.size(); ++var2) {
                    ((Polyline)this.mTrafficColorfulPolylines.get(var2)).setZIndex((float)var1);
                }
            }

            if(this.mDefaultPolyline != null) {
                this.mDefaultPolyline.setZIndex((float)var1);
            }
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }
}
