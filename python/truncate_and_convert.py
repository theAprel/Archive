from exceptions import KeyboardInterrupt
from xml.dom import minidom
from subprocess import call
import subprocess
import os
import sys

minutes_to_truncate = (39, 40, 41)
truncate_to = 32
output_dir = 'output HEVC'


class Duration:
    def __init__(self, timestamp='00:00:00'):
        parts = timestamp.split(':')
        if len(parts) != 3:
            raise ValueError("Invalid timestamp")
        self.hours = int(parts[0])
        self.minutes = int(parts[1])
        self.seconds = int(parts[2].split('.')[0])

    def is_hours_minutes(self, hours, minutes):
        return self.hours == hours and self.minutes == minutes

    def __str__(self):
        return str(self.hours) + 'h' + str(self.minutes) + 'm' + str(self.seconds) + 's'

    def should_truncate(self):
        for m in minutes_to_truncate:
            if self.is_hours_minutes(0, m):
                return True
        return False


def truncate_and_convert(is_animated, should_crop):
    xml = minidom.parse('METADATA.xml')
    filelist = xml.getElementsByTagName('FILE')
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    for f in filelist:
        to_be_truncated = False
        path = f.attributes['path'].value
        duration_xml = f.getElementsByTagName('DURATION_READABLE')
        if len(duration_xml) != 0:
            duration_string = duration_xml.item(0).childNodes[0].nodeValue
            duration = Duration(duration_string)
            if duration.should_truncate():
                to_be_truncated = True
        converted_file = output_dir + os.path.sep + path[:-3] + 'mkv'
        hvec_arg = ['ffmpeg', '-n', '-i', path, '-c:v', 'hevc_nvenc', '-profile:v', 'main10', '-preset',
                    'slow', '-rc', 'vbr', '-c:a', 'copy', '-sn', converted_file]
        if is_animated:
            # Intended for streams that have multi-audio; remove -map args otherwise
            animated_command = '-pixel_format p010le -map 0:v -map 0:a'
            animated_args = animated_command.split(' ')
            animated_args.reverse()
            for a in animated_args:
                hvec_arg.insert(8, a)
        if to_be_truncated:
            hvec_arg.insert(4, str(truncate_to) + ':00')
            hvec_arg.insert(4, '-t')
        if should_crop:
            hvec_arg.insert(4, 'crop=ih/3*4:ih')
            hvec_arg.insert(4, '-filter:v')
        # Check resolution as a proxy to determine whether interlaced and apply filtering accordingly
        is_progressive = False
        is_interlaced = False
        check_resoltion_process = subprocess.Popen(['ffprobe', path], stderr=subprocess.PIPE)
        for line in iter(check_resoltion_process.stderr.readline, ''):
            if '1280x720' in line:
                is_progressive = True
                break
            if '1920x1080' in line:
                is_interlaced = True
                break
        if not (is_progressive ^ is_interlaced):
            raise RuntimeError("Illegal state from ffprobe: is_interlaced: " + str(is_interlaced) + " is_progressive: "
                               + str(is_progressive) + ' for file ' + path)
        if is_interlaced:
            hvec_arg.insert(4, 'yadif=1')
            hvec_arg.insert(4, '-vf')
        print ' '.join(hvec_arg)
        try:
            call(hvec_arg)
        except KeyboardInterrupt:
            print "Process interrupted by user. Deleting incompletely converted file and exiting."
            os.remove(converted_file)
            sys.exit()
        except:
            print "Unrecognized exception during ffmpeg call."
            raise


if __name__ == "__main__":
    print "Usage: truncate_and_convert.py (must be called in directory with METADATA.xml)"
    print "Converts all files in METADATA.xml to HEVC and truncates to 32 minutes if they have specific length"
    print "Specify `anime` as argument for animated media, or `crop` to crop to 4:3"
    truncate_and_convert(len(sys.argv) > 1 and sys.argv[1] == 'anime', len(sys.argv) > 1 and sys.argv[1] == 'crop')
