from xml.dom import minidom
import os
import sys
from glob import glob
import codecs

def resize(filename, directory, md5sumFile):
    glob_files = glob(os.path.join(directory, u'**'), recursive=True)

    with codecs.open(filename, 'r', encoding='utf-8') as f:
        metadata_file_contents = f.read()
    xmldoc = minidom.parseString(metadata_file_contents)
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
        if f.getElementsByTagName('MD5').item(0) is None:  # In case the METADATA file was generated w/o checksums
            new_md5_element = xmldoc.createElement('MD5')
            text_node = xmldoc.createTextNode(md5[newPath])
            new_md5_element.appendChild(text_node)
            f.appendChild(new_md5_element)
        else:
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
        print("WARNING: The following files were expected by glob, but not found" + str(glob_files))
    with codecs.open(filename[:-4] + '-NEW.xml', 'w', encoding='utf-8') as writer:
        xmldoc.writexml(writer)

        
    
if __name__ == "__main__":
    print("Usage: resize.py <METADATA.xml file> <directory of files> <converted MD5 checksum file>")
    resize(sys.argv[1], sys.argv[2], sys.argv[3])

