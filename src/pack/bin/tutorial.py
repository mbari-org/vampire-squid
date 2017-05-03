import datetime
import json
import pprint
import random
import requests
import urllib
import uuid

def show(s, data = None):
    print("--- " + s)
    if data:
        pp = pprint.PrettyPringer(indent=2)
        pp.pprint(data)

def iso8601():
    return datatime.datetime.now(datetime.timezone.utc).isoformat()[0:-6] + "Z"

def parse_response(r):
    try:
        return json.loads(r.text)
    except:
        s = "URL: %s\n%s (%s): %s" % (r.request.url, r.status_code, r.reason, r.text)
        print(s)
        return {}

def delete(url):
    return parse_response(requests.delete(url))

def get(url):
    return parse_response(requests.get(url))

def post(url, data={}):
    return parse_response(requests.post(url, data))

def put(url, data={}):
    return parse_response(requests.put(url, data))

endpoint = "http://zen.shore.mbari.org:8080"
videosequence_url = "%s/v1/videosequences" % (endpoint)
video_url = "%s/v1/videos" % (endpoint)
videoreference_url = "%s/v1/videoreferences" % (endpoint)

# VideoSequence
videosequence = post(videosequence_url, data = {
    "name": "A Unique name",
    "camera_id": "Doc Ricketts",
    "description": "Some optional text"})
show("POST: " + videosequence_url, videosequence)

video = post(video_url, data = {
    "name": " A unique video name",
    "video_sequence_uuid": videosequence['uuid'],
    "start": iso8601(),
    "duration_millis": 15 * 60 * 1000,
    "description": "some description"})

videoref = post(videoreference_url, data = {
    "video_uuid": video['uuid']
    "uri": "http://url.or.uri/to/video.mp4",
    "description": "foo",
    "container": "quicktime mov",
    "video_codec": "h.264",
    "audio_codec": "aac",
    "width": 1920,
    "height": 1080,
    "frame_rate": 17
    "size_bytes": 50 * 1024 * 1024 * 1024
    ""
})



url = videosequence_url + "/" + videosequence['uuid']
videosequence = put(url, data = {
    "name": "Changed name",
    "camera_id": "Ventana",
    "description": "modified description"})
show("PUT: " + url, videosequence)

videosequences = get(videosequence_url)
show("GET: " + videosequence_url, data)

url = videosequence_url + "/" + videosequence['uuid']
videosequence = get(url)
show("GET: " + url, videosequence)

url = videosequence_url + "/name/" + videosequence['name']
videosequence = get(url)
show("GET: " + url, videosequence)

url = videosequence_url + "/names"
names = get(url)
show("GET: " + url, names)

url = videosequence_url + "/cameras"
cameras = get(url)
show("GET: " + url, cameras)





