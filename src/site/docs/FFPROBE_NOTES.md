# FFPROBE

# Example MP4

__Command__: 

`ffprobe -v quiet -show_streams -show_format -show_error -print_format json h265_test.mp4`

__Output:__

```
{
    "streams": [
        {
            "index": 0,
            "codec_name": "hevc",
            "codec_long_name": "HEVC (High Efficiency Video Coding)",
            "profile": "Main",
            "codec_type": "video",
            "codec_time_base": "1001/24000",
            "codec_tag_string": "hev1",
            "codec_tag": "0x31766568",
            "width": 3836,
            "height": 2156,
            "coded_width": 3840,
            "coded_height": 2160,
            "has_b_frames": 2,
            "sample_aspect_ratio": "1:1",
            "display_aspect_ratio": "137:77",
            "pix_fmt": "yuv420p",
            "level": 150,
            "color_range": "tv",
            "color_space": "bt709",
            "color_transfer": "bt709",
            "color_primaries": "bt709",
            "refs": 1,
            "r_frame_rate": "24000/1001",
            "avg_frame_rate": "31950000/1332581",
            "time_base": "1/90000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 1332581,
            "duration": "14.806456",
            "bit_rate": "5039267",
            "nb_frames": "355",
            "disposition": {
                "default": 1,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "creation_time": "2015-06-30 22:55:41",
                "language": "und",
                "handler_name": "VideoHandler"
            }
        }
    ],
    "format": {
        "filename": "h265_test.mp4",
        "nb_streams": 1,
        "nb_programs": 0,
        "format_name": "mov,mp4,m4a,3gp,3g2,mj2",
        "format_long_name": "QuickTime / MOV",
        "start_time": "0.000000",
        "duration": "14.807000",
        "size": "9335194",
        "bit_rate": "5043665",
        "probe_score": 100,
        "tags": {
            "major_brand": "mp42",
            "minor_version": "512",
            "compatible_brands": "isomiso2mp41",
            "creation_time": "2015-06-30 22:55:41",
            "encoder": "HandBrake 0.10.2 2015061100"
        }
    }
}
```

## Example ProRes #1

__Command__:

`ffprobe -v quiet -show_streams -show_format -show_error -print_format json 4k_ProRes_HQ_original.mov`

__Output__:

```
{
    "streams": [
        {
            "index": 0,
            "codec_name": "prores",
            "codec_long_name": "ProRes",
            "codec_type": "video",
            "codec_time_base": "1/24000",
            "codec_tag_string": "apch",
            "codec_tag": "0x68637061",
            "width": 3840,
            "height": 2160,
            "coded_width": 3840,
            "coded_height": 2160,
            "has_b_frames": 0,
            "sample_aspect_ratio": "1:1",
            "display_aspect_ratio": "16:9",
            "pix_fmt": "yuv422p10le",
            "level": -99,
            "color_space": "bt709",
            "color_transfer": "bt709",
            "color_primaries": "bt709",
            "refs": 1,
            "r_frame_rate": "24000/1001",
            "avg_frame_rate": "24000/1001",
            "time_base": "1/24000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 355355,
            "duration": "14.806458",
            "bit_rate": "629304821",
            "bits_per_raw_sample": "10",
            "nb_frames": "355",
            "disposition": {
                "default": 1,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "creation_time": "2013-10-03 23:38:00",
                "language": "eng",
                "handler_name": "Apple Alias Data Handler",
                "encoder": "Apple ProRes 422 (HQ)",
                "timecode": "00:00:00:00"
            }
        },
        {
            "index": 1,
            "codec_type": "data",
            "codec_time_base": "1/24",
            "codec_tag_string": "tmcd",
            "codec_tag": "0x64636d74",
            "r_frame_rate": "0/0",
            "avg_frame_rate": "0/0",
            "time_base": "1/24000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 355355,
            "duration": "14.806458",
            "bit_rate": "2",
            "nb_frames": "1",
            "disposition": {
                "default": 1,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "creation_time": "2013-10-03 23:39:57",
                "language": "eng",
                "handler_name": "Apple Alias Data Handler",
                "timecode": "00:00:00:00"
            }
        }
    ],
    "format": {
        "filename": "4k_ProRes_HQ_original.mov",
        "nb_streams": 2,
        "nb_programs": 0,
        "format_name": "mov,mp4,m4a,3gp,3g2,mj2",
        "format_long_name": "QuickTime / MOV",
        "start_time": "0.000000",
        "duration": "14.806458",
        "size": "1164734079",
        "bit_rate": "629311387",
        "probe_score": 100,
        "tags": {
            "major_brand": "qt  ",
            "minor_version": "537199360",
            "compatible_brands": "qt  ",
            "creation_time": "2013-10-03 23:38:00"
        }
    }
}
```

## Example ProRes #2

__Command__:

`ffprobe -v quiet -show_streams -show_format -show_error -print_format json 2015-06-15_Test_Dives_4.mov`

__Output__:

```
{
    "streams": [
        {
            "index": 0,
            "codec_name": "pcm_s24le",
            "codec_long_name": "PCM signed 24-bit little-endian",
            "codec_type": "audio",
            "codec_time_base": "1/48000",
            "codec_tag_string": "in24",
            "codec_tag": "0x34326e69",
            "sample_fmt": "s32",
            "sample_rate": "48000",
            "channels": 1,
            "bits_per_sample": 24,
            "r_frame_rate": "0/0",
            "avg_frame_rate": "0/0",
            "time_base": "1/48000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 37448611,
            "duration": "780.179396",
            "bit_rate": "1152000",
            "bits_per_raw_sample": "24",
            "nb_frames": "37464000",
            "disposition": {
                "default": 1,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "creation_time": "2015-06-15 21:20:00",
                "language": "eng",
                "handler_name": "Linux Alias Data Handler"
            }
        },
        {
            "index": 1,
            "codec_name": "pcm_s24le",
            "codec_long_name": "PCM signed 24-bit little-endian",
            "codec_type": "audio",
            "codec_time_base": "1/48000",
            "codec_tag_string": "in24",
            "codec_tag": "0x34326e69",
            "sample_fmt": "s32",
            "sample_rate": "48000",
            "channels": 1,
            "bits_per_sample": 24,
            "r_frame_rate": "0/0",
            "avg_frame_rate": "0/0",
            "time_base": "1/48000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 37448611,
            "duration": "780.179396",
            "bit_rate": "1152000",
            "bits_per_raw_sample": "24",
            "nb_frames": "37464000",
            "disposition": {
                "default": 1,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "creation_time": "2015-06-15 21:20:00",
                "language": "eng",
                "handler_name": "Linux Alias Data Handler"
            }
        },
        {
            "index": 2,
            "codec_name": "prores",
            "codec_long_name": "ProRes",
            "codec_type": "video",
            "codec_time_base": "1/60000",
            "codec_tag_string": "apch",
            "codec_tag": "0x68637061",
            "width": 2048,
            "height": 1080,
            "coded_width": 2048,
            "coded_height": 1080,
            "has_b_frames": 0,
            "sample_aspect_ratio": "1:1",
            "display_aspect_ratio": "256:135",
            "pix_fmt": "yuv422p10le",
            "level": -99,
            "color_space": "bt709",
            "color_transfer": "bt709",
            "color_primaries": "bt709",
            "refs": 1,
            "r_frame_rate": "60000/1001",
            "avg_frame_rate": "60000/1001",
            "time_base": "1/60000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 46810764,
            "duration": "780.179400",
            "bit_rate": "497877210",
            "bits_per_raw_sample": "10",
            "nb_frames": "46764",
            "disposition": {
                "default": 1,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "creation_time": "2015-06-15 21:20:00",
                "language": "eng",
                "handler_name": "Linux Alias Data Handler",
                "encoder": "Apple ProRes 422 (HQ)",
                "timecode": "21:20:00:06"
            }
        },
        {
            "index": 3,
            "codec_type": "data",
            "codec_time_base": "1/60",
            "codec_tag_string": "tmcd",
            "codec_tag": "0x64636d74",
            "r_frame_rate": "0/0",
            "avg_frame_rate": "0/0",
            "time_base": "1/60000",
            "start_pts": 0,
            "start_time": "0.000000",
            "duration_ts": 46810764,
            "duration": "780.179400",
            "nb_frames": "1",
            "disposition": {
                "default": 0,
                "dub": 0,
                "original": 0,
                "comment": 0,
                "lyrics": 0,
                "karaoke": 0,
                "forced": 0,
                "hearing_impaired": 0,
                "visual_impaired": 0,
                "clean_effects": 0,
                "attached_pic": 0
            },
            "tags": {
                "rotate": "0",
                "creation_time": "2015-06-15 21:20:00",
                "language": "eng",
                "handler_name": "AJA Time Code Handler",
                "reel_name": "001",
                "timecode": "21:20:00:06"
            }
        }
    ],
    "format": {
        "filename": "2015-06-15_Test_Dives_4.mov",
        "nb_streams": 4,
        "nb_programs": 0,
        "format_name": "mov,mp4,m4a,3gp,3g2,mj2",
        "format_long_name": "QuickTime / MOV",
        "start_time": "0.000000",
        "duration": "780.179400",
        "size": "48820544128",
        "bit_rate": "500608389",
        "probe_score": 100,
        "tags": {
            "major_brand": "qt  ",
            "minor_version": "537199360",
            "compatible_brands": "qt  ",
            "creation_time": "2015-06-15 21:20:00"
        }
    }
}
```