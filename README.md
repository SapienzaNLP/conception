<div align="center">    
 
# Conception: Multilingually-Enhanced, Human-Readable Concept Vector Representations     

[![Paper](http://img.shields.io/badge/paper-ACL--anthology-B31B1B.svg)](https://www.aclweb.org/anthology/2020.coling-main.291/)
[![Conference](http://img.shields.io/badge/COLING-2020-4b44ce.svg)](https://coling2020.org/)
[![License: CC BY-NC 4.0](https://img.shields.io/badge/License-CC%20BY--NC%204.0-lightgrey.svg)](https://creativecommons.org/licenses/by-nc/4.0/)

</div>

## Description
This is the repository for the paper [*Conception: Multilingually-Enhanced, Human-Readable Concept Vector Representations*](https://www.aclweb.org/anthology/2020.coling-main.291/),
presented at COLING 2020 by Simone Conia and Roberto Navigli.


## Abstract
> To date, the most successful word, word sense, and concept modelling techniques have used large
  corpora and knowledge resources to produce dense vector representations that capture semantic
  similarities in a relatively low-dimensional space. Most current approaches, however, suffer
  from a monolingual bias, with their strength depending on the amount of data available across
  languages. In this paper we address this issue and propose Conception, a novel technique for
  building language-independent vector representations of concepts which places multilinguality
  at its core while retaining explicit relationships between concepts. Our approach results in high-coverage
  representations that outperform the state of the art in multilingual and cross-lingual
  Semantic Word Similarity and Word Sense Disambiguation, proving particularly robust on lowresource languages.


## Download

### Code and Experiments
You can download a copy of all the files in this repository by cloning the
[git](https://git-scm.com/) repository:

    git clone https://github.com/SapienzaNLP/conception.git

or [download a zip archive](https://github.com/SapienzaNLP/conception/archive/master.zip).

### Vectors
* [Link to Google Drive (9GB)](https://drive.google.com/file/d/1o3hQc69qUeW-InUzfI2Qtc35AMX1XPDD/view?usp=sharing)

## Requirements
If you want to recreate or use the vectors, you will need the BabelNet APIs.
You can download all you need from the [official BabelNet website](https://babelnet.org/download).
We highly recommend to also download the BabelNet indices
(they are free to use for research purposes) to speed up the process.

## Cite this work
    @inproceedings{conia-and-navigli-2020-conception,
      title     = {{C}onception: {M}ultilingually-Enhanced, Human-Readable Concept Vector Representations},
      author    = {Conia, Simone and Navigli, Roberto},
      booktitle = {Proceedings of the 28th International Conference on Computational Linguistics, COLING 2020},
      year      = {2020}
    }
