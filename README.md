# radar-data-sim

A demo for streaming simulated, pre-processed radar data containing positional information for real-time analysis.

Example simulation output to Kafka:
```json
{"radarId":"North Truro FAA Long Range Radar Site","capturedTime":"2023-05-23T19:57:46.009084","entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","lat":40.84857,"lon":-67.70644}
{"radarId":"J-54","capturedTime":"2023-05-23T19:57:48.053402","entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","lat":40.841873,"lon":-67.7261}
{"radarId":"North Truro FAA Long Range Radar Site","capturedTime":"2023-05-23T19:57:56.017527","entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","lat":40.815754,"lon":-67.80262}
{"radarId":"J-54","capturedTime":"2023-05-23T19:57:58.039451","entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","lat":40.809113,"lon":-67.822044}
{"radarId":"North Truro FAA Long Range Radar Site","capturedTime":"2023-05-23T19:58:06.036915","entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","lat":40.78282,"lon":-67.89882}
{"radarId":"J-54","capturedTime":"2023-05-23T19:58:08.042950","entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","lat":40.77622,"lon":-67.91806}
```

Flink sliding window aggregation output:
```json
{"entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","capturedTime":"2023-05-23T19:57:58.039451","averageKnots":1726.4558,"capturedBy":["North Truro FAA Long Range Radar Site","J-54"]}
{"entityName":"13292ba6-83ca-4da2-b11f-2aa8ba0974a8","capturedTime":"2023-05-23T19:58:08.042950","averageKnots":1725.5243,"capturedBy":["North Truro FAA Long Range Radar Site","J-54"]}
```