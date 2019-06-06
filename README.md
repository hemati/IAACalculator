IAACalculator V0.0.6


The IAACalculator calculates the iaa score for two or more annotator's
using the Percentage-, RandolphKappa- or Bennett's agreement.


Infos:
The basic tsv file that contains the data must have the following 
structure or it won't work:

[sentenceID  verb  sentence  Annotator1  ......  AnnotatorN]


Furthermore it takes into account, that annotator's didn't
annotate all sentences equally, which results in a "*" at the specific cell
that should contain a sense number like "784421"(see below).

[id-s10-0	sagen	Gesagt, alle Mann und Frau getan.	12345	*	*	*	*	*	*	54321	*	]


The DKPro package is used to calculate the iaa scores,
the "*" changes to null values, via parsing, to match the needs addItem function
inside the CodingAnnotationStudy class.
E.g
[id-s10-0	sagen	Gesagt, alle Mann und Frau getan.	12345	null	null	null	null	null	null	54321	null	]


It should be mentioned that the Bennett's agreement is only for
2 Annotators.

-------------------------------------------------------------------------------------------------
>>>the programm was written in a few days and thats why some outputs have pairwise
agreement calculations for the first description row due to a sloppy coding, sorry for that!:)<<<
-------------------------------------------------------------------------------------------------

-------Starting------------------
(tested on windows, with JavaSE-1.8)
Input path should contain the name of the tsv file with .tsv at the end
in: ...\path\iaaFile.tsv
out: ...\path\folder

for the method just write the mentioned method name.
------------------------------------------------------
