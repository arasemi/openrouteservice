/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.isochrones;

import heigit.ors.common.TravelRangeType;
import heigit.ors.common.TravellerInfo;
import heigit.ors.services.ServiceRequest;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class IsochroneRequest extends ServiceRequest
{
	private List<TravellerInfo> _travellers;
	private String _calcMethod;
	private String _units = null;
	private Boolean _includeIntersections = false;
	private String[] _attributes;

	public IsochroneRequest()
	{
		_travellers = new ArrayList<TravellerInfo>();
	}

	public String getCalcMethod() 
	{
		return _calcMethod;
	}

	public void setCalcMethod(String calcMethod) 
	{
		_calcMethod = calcMethod;
	}

	public String getUnits() {
		return _units;
	}

	public void setUnits(String units) {
		_units = units;
	}
	
	public boolean isValid()
	{
		return _travellers.size() >= 1;
	}

	public String[] getAttributes() {
		return _attributes;
	}

	public void setAttributes(String[] _attributes) {
		this._attributes = _attributes;
	}

	public boolean hasAttribute(String attr) {
		if (_attributes == null || attr == null)
			return false;

		for (int i = 0; i < _attributes.length; i++)
			if (attr.equalsIgnoreCase(_attributes[i]))
				return true;

		return false;
	}

	public Boolean getIncludeIntersections()
	{
		return _includeIntersections;
	}

	public void setIncludeIntersections(Boolean value)
	{
		_includeIntersections = value;
	}
	
	public Coordinate[] getLocations()
	{
		Coordinate[] locations = new Coordinate[_travellers.size()];
		
		for(int i = 0; i < _travellers.size(); i++)
		{
			locations[i] = _travellers.get(i).getLocation();
		}
		  
		return locations;
	}

	public IsochroneSearchParameters getSearchParameters(int travellerIndex)
	{
		TravellerInfo traveller = _travellers.get(travellerIndex);
		double[] ranges = traveller.getRanges();

		// convert ranges in units to meters or seconds
		if (!(_units == null || "m".equalsIgnoreCase(_units)))
		{
			double scale = 1.0;
			if (traveller.getRangeType() == TravelRangeType.Distance)
			{
				switch(_units)
				{
				case "m":
					break;
				case "km":
					scale = 1000;
					break;
				case "mi":
					scale = 1609.34;
					break;
				}
			}

			if (scale != 1.0)
			{
				for (int i = 0; i < ranges.length; i++)
					ranges[i] = ranges[i]*scale;
			}
		}

		IsochroneSearchParameters parameters = new IsochroneSearchParameters(travellerIndex, traveller.getLocation(), ranges);
		parameters.setLocation(traveller.getLocation());
		parameters.setRangeType(traveller.getRangeType() );
		parameters.setCalcMethod(_calcMethod);
		parameters.setRouteParameters(traveller.getRouteSearchParameters());
		if ("destination".equalsIgnoreCase(traveller.getLocationType()))
			parameters.setReverseDirection(true);

		return parameters;
	}

	public List<TravellerInfo> getTravellers() {
		return _travellers;
	}
	
	public void addTraveller(TravellerInfo traveller) throws Exception
	{
		if (traveller == null)
			throw new Exception("'traveller' argument is null.");
		
		_travellers.add(traveller);
	}
}
