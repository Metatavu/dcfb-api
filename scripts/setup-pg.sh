#!/bin/sh

sudo -u postgres createuser -R -S dcfbtest
sudo -u postgres createdb -Odcfbtest -Ttemplate0 dcfbtest
sudo -u postgres psql -c "alter user dcfbtest with password 'dcfbtest'"
sudo -u postgres psql -c "alter role dcfbtest with login"