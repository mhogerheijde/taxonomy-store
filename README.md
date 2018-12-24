Taxonomy store
==============

This project presumes a basic knowledge about what [taxonmies](https://www.xbrl.org) are.

The Taxonomy-store aims to make a few things possible:

1. Retrieval of taxonomy-documents from a trusted source
   Taxonomies are not always provided at the URLs they use,
   and also: the URLs might not be https.
   This project aims to provide a way to get the documents from a trusted store.
2. Optimised delivery of taxonomy-documents
   [DTS-discovery](http://www.xbrl.org/Specification/XBRL-2.1/REC-2003-12-31/XBRL-2.1-REC-2003-12-31+corrected-errata-2013-02-20.html#_3)
   means following loads of links across the documents.
   This project aims to provide a pre-processed set of documents, 
   so the client can just start using the documents.
   Also: just following links and doing web-calls can be slow.
   Using gRPC and Protobuf increases the performance of lookups.
3. Easier delivery of taxonomies across nodes.
   When you want your nodes to read from a trusted taxonomy,
   you'd need a custom solution to get your taxonomy at that node.
   One solution is just copying it over to all nodes that need it.
   When you need to manage your taxonomies and you don't want to publish them online,
   you can use this solution to place them in the store and read from the store with clients.


API
---

The API is defined in protobuf.

Server
------

The server implements the protobuf definition using gRPC.

TODO: The server should be packaged as docker image.


Client
------

The client provides a way to retrieve entire taxonomies (per DTS) and or separate taxonomy documents. 
The way they are provided is using `UriResolver`s.
Basically `UriResolver`s take in some `URI` and provide the corresponding `org.xml.sax.InputSource`.

The client project contains a client-implementation on the API defined in protobuf. 
One can, of course, create their own implementation using the protobuf definition.
 
