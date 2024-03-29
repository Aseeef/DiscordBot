package net.grandtheftmc.discordbot.commands.stats.wrappers;

import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.TimeZone;

public class WrappedIPData {

    private final String ip;
    private final String asn;
    private final String provider;
    private final String country;
    private final String isocode;
    private final String city;
    private final String region;
    private final String regioncode;
    private final Double latitude;
    private final Double longitude;
    private final TimeZone timezone;
    private final boolean proxy;
    private final String type;
    private final byte risk;
    private final Timestamp lastUpdated;

    public WrappedIPData(String ip, String asn, @Nullable String provider, @Nullable String country, @Nullable String isocode, @Nullable String city, @Nullable String region, @Nullable String regioncode, @Nullable Double latitude, @Nullable Double longitude, @Nullable TimeZone timezone, boolean proxy, String type, byte risk, Timestamp lastUpdated) {
        this.ip = ip;
        this.asn = asn;
        this.provider = provider;
        this.country = country;
        this.isocode = isocode;
        this.city = city;
        this.region = region;
        this.regioncode = regioncode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.proxy = proxy;
        this.type = type;
        this.risk = risk;
        this.lastUpdated = lastUpdated;
    }

    public String getIp() {
        return ip;
    }

    public String getAsn() {
        return asn;
    }

    public @Nullable String getProvider() {
        return provider;
    }

    public @Nullable String getCountry() {
        return country;
    }

    public @Nullable String getIsocode() {
        return isocode;
    }

    public @Nullable String getCity() {
        return city;
    }

    public @Nullable String getRegion() {
        return region;
    }

    public @Nullable String getRegioncode() {
        return regioncode;
    }

    public @Nullable Double getLatitude() {
        return latitude;
    }

    public @Nullable Double getLongitude() {
        return longitude;
    }

    public @Nullable TimeZone getTimezone() {
        return timezone;
    }

    public byte getRisk() {
        return risk;
    }

    public boolean isProxy() {
        return proxy;
    }

    public String getType() {
        return type;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return ip + "[asn='" + asn + "',provider='" + provider + "',country='" + country + "',isocode='" + isocode + "',city='" + city + "',region='" + region + "',regioncode='" + regioncode + "',latitude='" + latitude + "',longitude='" + longitude + "',proxy='" + proxy + "',type='" + type + "',updated='" + lastUpdated.toString() +"']";
    }

}
