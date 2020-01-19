#!/bin/bash

TAR=tar

echo `uname`

if [ `uname` = "SunOS" ]
then
echo "On SunOs using gtar instead of tar."
TAR=gtar
fi

if [ `uname` = "Darwin" ]
then
echo "On MacOS using tar."
fi


# create lib folder if it doesn't exist
if [ ! -d lib ];
then
   mkdir lib
fi

# download lola version 1.14 if it doesn't exist
VERSION=1.14
if [ -e lola-1.14 ];
then
    echo "Lola 1.14 source directory exists, continuing..."
else 
  wget http://download.gna.org/service-tech/lola/lola-${VERSION}.tar.gz
  $TAR zxf lola-${VERSION}.tar.gz
#  ln -s lola-${VERSION} lola
  cd lola-${VERSION}
  ./configure
  cd ..
fi

# download lola version 1.16 if it doesn't exist
VERSION=1.16
if [ -e lola-1.16 ];
  then
      echo "Lola 1.16 source directory exists, continuing..."
  else
  wget http://download.gna.org/service-tech/lola/lola-${VERSION}.tar.gz
  $TAR zxf lola-${VERSION}.tar.gz
  cd lola-${VERSION}
  ./configure
  cd ..
fi

# build lola (full) for reachability graph in version 1.14
# this is a workaround, because version 1.16 has a bug preventing
# the creation of reachability graphs
LOLADIR=lola-1.14
echo Directory is $LOLADIR
if [ -e "lib/lola" -a "`lib/lola -V`" = "LoLA 1.14" ];
then 
    echo "========================================================"
    echo "Correct version (1.14) of lola (full) binary exists; skipping"
else
    echo "========================================================"
    echo "Wrong or no version of lola (full) binary detected; compiling"

    cd $LOLADIR
    cp ../userconf/lola/userconfig.H src
    make clean
    make
    cp src/lola ../lib/lola
    cd ..
fi

# build other lola binaries in version 1.16
LOLADIR=lola-1.16
echo Directory is $LOLADIR
pwd
cd $LOLADIR

for F in `ls ../userconf` 
  do
    if [ "$F" == "lola" ];
    then
	echo "skipping lola (full), was already compiled."
    else
	if [[ -e "../lib/$F" && "`../lib/$F -V`" == *$VERSION ]] 
	then 
	    echo "========================================================"
	    echo "Skipping $F"
	else
	    echo "========================================================"
	    echo "compiling $F ..."

	    cp ../userconf/$F/userconfig.H src
	    make clean
	    make
	    cp src/lola ../lib/$F
	fi
    fi
done
cd ..

# make graph2dot program
if [ ! -e lib/graph2dot ];
then
    cd $LOLADIR/utils
    make clean
    make
    cp graph2dot ../../lib/
else
    echo "========================================================"
    echo "Skippint graph2dot, it already exists in lib folder"
fi
