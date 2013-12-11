package com.trackmars.and.tracker;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

class Rectangle {
	
	private Double left;
	private Double right;
	private Double top;
	private Double bottom;
	
	private boolean altered = false;
	
	public boolean isAltered() {
		return altered;
	}

	public void setAltered(boolean altered) {
		this.altered = altered;
	}

	
	public void create (Double left, Double right, Double top, Double bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.altered = false;
	}
	
	public boolean shape (Location location) {
		LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
		return shape(latLng);
	}
		
	public boolean shape (LatLng location) {

		if (left != null) {
			if (left > location.longitude) {
				left = location.longitude;
				altered = true;
			}
		} else { 
			left = location.longitude;
			altered = true;
		}
		
		if (right != null) {
			if (right < location.longitude) {
				right = location.longitude;
				altered = true;
			}
		} else { 
			right = location.longitude;
			altered = true;
		}
		
		if (top != null) {
			if (top < location.latitude) {
				top = location.latitude;
				altered = true;
			}
		} else { 
			top = location.latitude;
			altered = true;
		}
		
		if (bottom != null) {
			if (bottom > location.latitude) {
				bottom = location.latitude;
				altered = true;
			}
		} else { 
			bottom = location.latitude;
			altered = true;
		}
		
		return altered;
	}
	
	public Double getLeft() {
		return left;
	}
	public void setLeft(Double left) {
		this.left = left;
		altered = true;
	}
	public Double getRight() {
		return right;
	}
	public void setRight(Double right) {
		this.right = right;
		altered = true;
	}
	public Double getTop() {
		return top;
	}
	public void setTop(Double top) {
		this.top = top;
		altered = true;
	}
	public Double getBottom() {
		return bottom;
	}
	public void setBottom(Double bottom) {
		this.bottom = bottom;
		altered = true;
	}
	
	
}

