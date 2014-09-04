package com.trackmars.and.tracker.dataUtils;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import ru.elifantiev.android.roboerrorreporter.Logger;
import android.content.Context;
import android.os.Environment;

import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.TrkPtType;
import com.topografix.gpx._1._1.TrkType;
import com.topografix.gpx._1._1.TrksegType;
import com.topografix.gpx._1._1.WptType;
import com.trackmars.and.tracker.TrackRecorderService;
import com.trackmars.and.tracker.model.Point;
import com.trackmars.and.tracker.model.Track;
import com.trackmars.and.tracker.model.TrackPointData;

public class XMLFile { 
	private GpxType objectsSet;
	
	private Context context;
	
	public XMLFile (Context context) {
		this.context = context;
		objectsSet = new GpxType();
	}
	
	
	public void serialize(String fileName) throws Exception {
		
		Logger.log("serialize");
		
		Serializer serializer = new Persister();

		
		String stacktraceDir = String.format("/Android/data/%s/files/", context.getPackageName());
		Logger.log("stacktraceDir " + stacktraceDir);
		
        File sd = Environment.getExternalStorageDirectory();

        File result = new File(sd.getPath() + stacktraceDir, "out.gpx");
		Logger.log("file opend result=" + result);
		serializer.write(objectsSet, result);
		Logger.log("wrote ");
	}
	
	public void addTrack(Track track) throws IllegalAccessException, InstantiationException {
		
		TrkType trk = new TrkType();
		trk.setName(track.TITLE);
		
		objectsSet.setCreator("Trackmars.com");
		objectsSet.setVersion("1.1");
		
		List<TrackPointData> trackPoints = TrackRecorderService.getAllTrackPointByTrack(track.ID, context);
		
		TrksegType trkSeg = new TrksegType();;
		
		Logger.log("addTrack");
		
		List<IEntity> points = DataOperation.getPointsByTrack(context, track.ID);
		
		Point point = null;
		
		if (points != null && points.size() > 0) {
			point = (Point) points.get(0);
		}
		
		while (points != null && points.size() > 0 && point != null) {
			
			WptType wptType = new WptType();
			
			wptType.setTime(new Date(point.COLUMN_CREATED));
			
			wptType.setLat(new BigDecimal(point.COLUMN_LAT));
			wptType.setLon(new BigDecimal(point.COLUMN_LNG));
			wptType.setName(point.COLUMN_TITLE);
			wptType.setDesc(point.COLUMN_TITLE);
			wptType.setType(point.COLUMN_ID_TYPE.toString());
			
			objectsSet.getWpt().add(wptType);
			
			points.remove(point);
			
			if (points.size() > 0) {
				point = (Point) points.get(0);
			}
			
		}
		
		for (TrackPointData trackPointData : trackPoints) {
			
			TrkPtType trkptType = new TrkPtType();
			
			trkptType.setTime(new Date(trackPointData.CREATED));
			
			trkptType.setLat(new BigDecimal(trackPointData.LAT));
			trkptType.setLon(new BigDecimal(trackPointData.LNG));
			
			trkSeg.getTrkpt().add(trkptType);
			if (trackPointData.paused) {
				trk.getTrkseg().add(trkSeg);
			}
			
		}
		
		objectsSet.getTrk().add(trk);
		
	}
	
}
