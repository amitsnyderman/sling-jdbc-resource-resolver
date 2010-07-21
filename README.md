Generic JDBC ResourceResolver Implementation for Apache Sling
=============================================================

Maps URLs to SQL queries based on a configured routing URL, e.g.: /path/to/root/:table/:id

The following URL:

	/path/to/root/items/1

...becomes:

	SELECT * FROM `items` WHERE `id`=1

And makes selected rows available as a Sling `Resource`.