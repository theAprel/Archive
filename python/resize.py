from xml.dom import minidom
import os
import sys
from glob import glob
import codecs

def resize(filename, directory):
    glob_files = glob(os.path.join(directory, u'*'))

    xmldoc = minidom.parse(filename)
    xml_files = xmldoc.getElementsByTagName('FILE')
    for f in xml_files:
        path = f.attributes['path'].value
        f.getElementsByTagName('SIZE').item(0).childNodes[0].nodeValue = os.path.getsize(os.path.join(directory, path))
        print f.getElementsByTagName('MD5').item(0).childNodes[0].nodeValue, '  ', path
        found_it = False
        for full_path in glob_files[:]:
            if full_path.endswith(path):
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
    print "Usage: resize.py <METADATA.xml file> <directory of files>"
    resize(sys.argv[1], sys.argv[2])

