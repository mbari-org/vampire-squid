#!/usr/bin/env python

# This is a script for loading some test data into the video-asset-manager using
# REST calls. Each insert is being done as a separate call, which is the only type of
# insert that the API supports at this point.
#
# Note that for production loads, we would not do this in this manner. Instead, we might
# use a script that takes a few parameters (e.g. file URL, camera_id) then
# - auto-generates some parameters, such as video-sequence name and video name.
# - parses the other metadata from the video file as json, using something like ffprobe
# - maps the json from ffprobe to the asset managers metadata.
# - generates the video-sequence and video objects via API's calls as needed.

import requests
import json

def read(url, data = {}):
    r = requests.post(url, data)
    return json.loads(r.text)

base_url = "http://localhost:8080/v1/"
vs_url = base_url + "videosequence"
v_url = base_url + "video"
vr_url = base_url + "videoreference"

vs1 = read(vs_url, 
        data = {"name": "T0097", 
                "camera_id": "Tiburon"})
v1_1 = read(v_url,
        data = {"name": "T0097-01", 
                "start": "2016-04-05T00:01:00Z",
                "duration_millis": 15 * 60 * 1000, 
                "video_sequence_uuid": vs1["uuid"]})
vr1_1_1 = read(vr_url,
        data = {"uri": "http://www.mbari.org/foo/bar/T0097_20160405T000100Z.mov",
                "video_uuid": v1_1["uuid"],
                "container": "video/quicktime", 
                "videocodec": "ProRes HQ",
                "audioCodec": "AAC",
                "width": 1920, 
                "height": 1080,
                "frame_rate": 59.97})                
vr1_1_2 = read(vr_url,
        data = {"uri": "http://www.mbari.org/foo/bar/T0097_20160405T000100Z.mp4",
                "video_uuid": v1_1["uuid"],
                "container": "video/mp4",
                "videocodec": "H.264", 
                "audioCodec": "AAC",
                "width": 1920, 
                "height": 1080,
                "frame_rate": 59.97}) 
        
v1_2 = read(v_url,
        data = {"name": "T0097-02", 
                "start": "2016-04-05T00:01:15Z",
                "duration_millis": 15 * 60 * 1000, 
                "video_sequence_uuid": vs1["uuid"],
                "description": "This video is cool"})
vr1_2_1 = read(vr_url,
        data = {"uri": "http://www.mbari.org/foo/bar/T0097_20160405T000115Z.mov",
                "video_uuid": v1_2["uuid"],
                "container": "video/quicktime", 
                "videocodec": "ProRes HQ",
                "audioCodec": "AAC",
                "width": 1920, 
                "height": 1080,
                "frame_rate": 59.97})                
vr1_2_2 = read(vr_url,
        data = {"uri": "http://www.mbari.org/foo/bar/T0097_20160405T000115Z.mp4",
                "video_uuid": v1_2["uuid"],
                "container": "video/mp4",
                "videocodec": "H.264", 
                "audioCodec": "AAC",
                "width": 1920, 
                "height": 1080,
                "frame_rate": 59.97})

v1_3 = read(v_url,
            data = {"name": "T0097-01HD",
                    "start": "2016-04-05T00:01:00Z",
                    "duration_millis": 45 * 60 * 1000,
                    "video_sequence_uuid": vs1["uuid"],
                    "description": "This is a reference to a tape that overlaps with video files"})
vr1_3_1 = read(vr_url,
         data = {"uri": "urn:T0097-01HD",
                 "video_uuid": v1_3["uuid"],
                 "width": 1920,
                 "height": 1080,
                 "frame_rate": 29.97,
                 "description": "D5 Tape"})
        
         
        
vs2 = read(vs_url, 
        data = {"name": "V1234", "camera_id": "Ventana"})
vs3 = read(vs_url, 
        data = {"name": "V9931", "camera_id": "Ventana"})
        


