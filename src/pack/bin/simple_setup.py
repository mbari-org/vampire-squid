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
                "video_codec": "ProRes HQ",
                "audio_codec": "AAC",
                "width": 1920,
                "height": 1080,
                "frame_rate": 59.97})
vr1_1_2 = read(vr_url,
        data = {"uri": "http://www.mbari.org/foo/bar/T0097_20160405T000100Z.mp4",
                "video_uuid": v1_1["uuid"],
                "container": "video/mp4",
                "video_codec": "H.264",
                "audio_codec": "AAC",
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
                "video_codec": "ProRes HQ",
                "audio_codec": "AAC",
                "width": 1920,
                "height": 1080,
                "frame_rate": 59.97})
vr1_2_2 = read(vr_url,
        data = {"uri": "http://www.mbari.org/foo/bar/T0097_20160405T000115Z.mp4",
                "video_uuid":v1_2["uuid"],
                "container":"video/mp4",
                "video_codec":"H.264",
                "audio_codec":"AAC",
                "width":1920,
                "height":1080,
                "frame_rate":19})

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
v2_1 = read(v_url,
            data = {"name": "V1234-01",
                    "start": "2016-06-12T00:18:31Z",
                    "duration_millis": 15 * 60 * 1000,
                    "video_sequence_uuid": vs2["uuid"]})
vr2_1_1 = read(vr_url,
               data = {"uri": "http://www.mbari.org/foo/bar/V1234_20160612T001831.mov",
                       "video_uuid": v2_1["uuid"],
                       "container": "video/quicktime",
                       "video_codec": "ProRes HQ",
                       "audio_codec": "AAC",
                       "width": 1920,
                       "height": 1080,
                       "frame_rate": 59.97})
vr2_1_2 = read(vr_url,
               data = {"uri": "http://www.mbari.org/foo/bar/V1234_20160612T001831.mp4",
                       "video_uuid": v2_1["uuid"],
                       "container": "video/mp4",
                       "video_codec": "H.264",
                       "audio_codec": "AAC",
                       "width": 1920,
                       "height": 1080,
                       "frame_rate": 19})

vs3 = read(vs_url,
        data = {"name": "V9931", "camera_id": "Ventana"})
v3_1 = read(v_url,
            data = {"name": "V9931-01",
                    "start": "2011-12-12T00:00:10Z",
                    "duration_millis": 45 * 60 * 1000,
                    "video_sequence_uuid": vs3["uuid"]})
vr3_1_1 = read(vr_url,
               data = {"uri": "http://www.mbari.org/foo/bar/V9931_201101212T000010Z.mov",
                       "video_uuid": v3_1["uuid"],
                       "container": "video/quicktime",
                       "video_codec": "ProRes HQ",
                       "audio_codec": "AAC",
                       "width": 1920,
                       "height": 1080,
                       "frame_rate": 59.97})
vr3_1_2 = read(vr_url,
               data = {"uri": "http://www.mbari.org/foo/bar/V9931_201101212T000010Z.mp4",
                       "video_uuid": v3_1["uuid"],
                       "container": "video/mp4",
                       "video_codec": "H.264",
                       "audio_codec": "AAC",
                       "width": 1920,
                       "height": 1080,
                       "frame_rate": 30})
vr3_1_3 = read(vr_url,
               data = {"uri": "http://www.mbari.org/foo/bar/V9931_201101212T000010Z_midres.mp4",
                       "video_uuid": v3_1["uuid"],
                       "container": "video/mp4",
                       "video_codec": "H.264",
                       "audio_codec": "AAC",
                       "width": 720,
                       "height": 640,
                       "frame_rate": 19})
