import { MapContainer, TileLayer, Marker, Popup, Circle, Tooltip } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';

const normalIcon = L.divIcon({
  html: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="26" height="26" fill="#4b5563">
    <path d="M21,16v-2l-8-5V3.5C13,2.67,12.33,2,11.5,2S10,2.67,10,3.5V9l-8,5v2l8-2.5V19l-2,1.5V22l3.5-1l3.5,1v-1.5L13,19v-5.5L21,16z"/>
  </svg>`,
  className: '',
  iconSize: [26, 26],
  iconAnchor: [13, 13],
  popupAnchor: [0, -13]
});

const alertIcon = L.divIcon({
  html: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="30" height="30" fill="#eab308">
    <path d="M21,16v-2l-8-5V3.5C13,2.67,12.33,2,11.5,2S10,2.67,10,3.5V9l-8,5v2l8-2.5V19l-2,1.5V22l3.5-1l3.5,1v-1.5L13,19v-5.5L21,16z"/>
  </svg>`,
  className: '',
  iconSize: [30, 30],
  iconAnchor: [15, 15],
  popupAnchor: [0, -15]
});

export default function DroneMap({ aircraft, zones, alertIcaos }) {
  return (
    <MapContainer center={[40.4, -3.7]} zoom={6}
      style={{ height: '500px', width: '100%', borderRadius: '8px' }}>
      <TileLayer url='https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
        attribution='OpenStreetMap contributors' />
      {zones.map(z => {
        const color = z.type === 'AIRPORT' ? '#3b82f6' : z.type === 'NUCLEAR' ? '#f97316' : '#dc2626';
        return (
          <Circle key={z.id} center={[z.latitude, z.longitude]}
            radius={z.radiusKm * 1000}
            pathOptions={{ color, fillColor: color, fillOpacity: 0.12 }}>
            <Popup>{z.name} — {z.type}</Popup>
          </Circle>
        );
      })}
      {aircraft.filter(a => a.latitude && a.longitude).map(a => (
        <Marker key={`${a.icao24}-${alertIcaos.has(a.icao24)}`} position={[a.latitude, a.longitude]}
          icon={alertIcaos.has(a.icao24) ? alertIcon : normalIcon}>
          <Tooltip direction="top" offset={[0, -14]} opacity={0.9}>
            <strong>{a.callsign || a.icao24}</strong>
          </Tooltip>
          <Popup>
            <strong>{a.callsign}</strong><br/>
            {alertIcaos.has(a.icao24) &&
              <span style={{color:'red',fontWeight:'bold'}}>ALERTA ACTIVA</span>}
            <br/>Alt: {a.altitude ? Math.round(a.altitude) + ' m' : 'N/A'}
            <br/>Vel: {a.velocity ? Math.round(a.velocity * 3.6) + ' km/h' : 'N/A'}
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}
