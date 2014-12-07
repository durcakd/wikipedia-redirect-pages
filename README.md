wikipedia
=========

Parsing of useful data from Wikipedia, DBPedia and Freebase 

## Project: Redirect pages 

### Requirements

To run this project, you need to have 
* Java (1.6 or more)
* Hadoop (2.5.1 or more)
All needed libraries for Hadoop and Lucene are included in  data/lib/ directory

### Installation

First, clone the repository.
```
git clone https://github.com/durcakd/wikipedia.git
cd wikipedia
```

### Data

* Example data included in test/ directory
* Download wikipedia dump skwiki-latest-pages-articles.xml
from http://dumps.wikimedia.org/skwiki/latest/ 

### Run

Compile and run Java GUI aplication and:
* choose input file to parse - output file will be created
* choose output file from parsing to index - index will be created 
* search

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request


