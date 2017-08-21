from xml.dom import minidom
import os
import sys
from glob import glob
import codecs

def resize(filename, directory, md5sumFile):
    glob_files = glob(os.path.join(directory, u'*'))

    xmldoc = minidom.parse(filename)
    xml_files = xmldoc.getElementsByTagName('FILE')

    md5 = {}
    with codecs.open(md5sumFile, 'r', encoding='utf-8') as f:
        content = [x.strip('\n') for x in f.readlines()]
    for line in content:
        parts = line.split('  ')
        md5[parts[1]] = parts[0]

    for f in xml_files:
        path = f.attributes['path'].value
        newPath = path[:-3] + 'mkv'
        f.attributes['path'].value = newPath
        f.getElementsByTagName('SIZE').item(0).childNodes[0].nodeValue = os.path.getsize(os.path.join(directory, newPath))
        f.getElementsByTagName('MD5').item(0).childNodes[0].nodeValue = md5[newPath]
        found_it = False
        for full_path in glob_files[:]:
            if full_path.endswith(newPath):
                glob_files.remove(full_path)
                found_it = True
                break #in case of dup file names over dirs, remove only one
        if not found_it:
            raise ValueError('No matching file was found on glob path', path)
    if len(glob_files) != 0:
        raise AssertionError('Not all files found: ' + str(glob_files))
    with codecs.open(filename[:-4] + '-NEW.xml', 'w', encoding='utf-8') as writer:
        xmldoc.writexml(writer)

        
    
if __name__ == "__main__":
    print "Usage: resize.py <METADATA.xml file> <directory of files> <converted MD5 checksum file>"
    resize(sys.argv[1], sys.argv[2], sys.argv[3])

