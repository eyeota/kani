insert into kani_test.bands (id, albums, best_hit_by_year, created, description, double_value, float_value, id_list, ip_address, members, name, photo, total_album_sales)
values (
	327b75d6-d76f-4375-b7e9-d65066dfbd4b,
	['Permission to Land', 'One Way Ticket to Hell and Back', 'Hot Cakes', 'Last of Our Kind'],
	{2003: 'I Believe in a Thing Called Love', 2005: 'One Way Ticket to Hell and Back', 2012: 'Every Inch of You', 2015: 'Barbarian'},
	1468382192000,
	'Description that uses newline
Okay next line
	Yeah next line and tab',
	1.7976931348623157E308,
	3.4028235E38,
	[9cdb1ff0-fe66-42ff-8fb9-f9a0aefcafe5, 2a825c22-1316-4f83-a5c6-de5bb20fbda4, 38851df1-8bdf-4971-a1fa-85c59a6d4f67],
	'127.0.0.1',
	{'Justin Hawkins', 'member 2', 'member 3', 'member 4'},
	'The Darkness',
	bigintAsBlob(3),
	9999999999999991
);

insert into kani_test.bands (id, albums, best_hit_by_year, created, description, double_value, float_value, id_list, ip_address, members, name, photo, total_album_sales)
values (
	01f4264f-d1f2-4b47-98b3-5fec72619834,
	['album1', 'album2'],
	{2016: 'awesome'},
	1468382392000,
	'description',
	1.7976931348623157E308,
	3.4028235E38,
	[d0ef3732-1dec-44ed-a96a-ea8ed72c1553],
	'10.0.0.1',
	{'member x', 'member y'},
	'Dude',
	bigintAsBlob(5),
	9999999999999992
);

insert into kani_test.bands (id, albums, best_hit_by_year, created, description, double_value, float_value, id_list, ip_address, members, name, photo, total_album_sales)
values (
	01f4264f-d3f3-4b47-98b3-5fec72619834,
	null,
	null,
	null,
	null,
	null,
	null,
	null,
	null,
	null,
	null,
	null,
	null
);


insert into kani_test.playlists (artist_id, mood, created, name, songs)
values (
	327b75d6-d76f-4375-b7e9-d65066dfbd4b,
	'sexy',
	now(),
	'sexy playlist',
	['I Believe in a Thing Called Love', 'Love is Only a Feeling', 'Mr. Boombastic']
);
insert into kani_test.playlists (artist_id, mood, created, name, songs)
values (
	327b75d6-d76f-4375-b7e9-d65066dfbd4b,
	'awesome',
	now(),
	'awesome playlist',
	['Sexy and I Know It']
);
insert into kani_test.playlists (artist_id, mood, created, name, songs)
values (
	01f4264f-d1f2-4b47-98b3-5fec72619834,
	'sexy',
	now(),
	'sexy playlist',
	['Hurr durr theres a double quote "whee"']
);

insert into kani_test.tracks_by_album(album_title, year, number ,band, genre, track_title)
values (
	'Permission to Land',
	2013,
	1,
	'The Darkness',
	'Rock',
	'I Believe in a Thing Called Love'
);

insert into kani_test.tracks_by_album(album_title, year, number ,band, genre, track_title)
values (
	'Permission to Land',
	2013,
	2,
	'The Darkness',
	'Rock',
	'Love is Only a Feeling'
);

insert into kani_test.tracks_by_album(album_title, year, number ,band, genre, track_title)
values (
	'Permission to Land',
	2013,
	3,
	'The Darkness',
	'Rock',
	'Stuck in a Rut'
);

insert into kani_test.tracks_by_album(album_title, year, number ,band, genre, track_title)
values (
	'album1',
	2015,
	1,
	'Dude',
	'Pop',
	'song1'
);

insert into kani_test.tracks_by_album(album_title, year, number ,band, genre, track_title)
values (
	'album1',
	2015,
	21,
	'Dude',
	'Pop',
	'song2'
);

insert into kani_test.tracks_by_album(album_title, year, number ,band, genre, track_title)
values (
	'album1',
	2015,
	3,
	'Dude',
	'Pop',
	'song3'
);
