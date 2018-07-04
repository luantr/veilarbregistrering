ALTER TABLE BRUKER_REGISTRERING add ANDRE_UTFORDRINGER varchar(30);
ALTER TABLE BRUKER_REGISTRERING add BEGRUNNELSE_FOR_REGISTRERING varchar(30);
ALTER TABLE BRUKER_REGISTRERING add UTDANNING_BESTATT varchar(30);
ALTER TABLE BRUKER_REGISTRERING add UTDANNING_GODKJENT_NORGE varchar(30);

UPDATE BRUKER_REGISTRERING
SET ANDRE_UTFORDRINGER='-', BEGRUNNELSE_FOR_REGISTRERING='-', UTDANNING_BESTATT='-', UTDANNING_GODKJENT_NORGE='-'
WHERE ANDRE_UTFORDRINGER IS NULL AND BEGRUNNELSE_FOR_REGISTRERING IS NULL AND UTDANNING_BESTATT IS NULL AND UTDANNING_GODKJENT_NORGE IS NULL;

ALTER TABLE BRUKER_REGISTRERING MODIFY YRKESBESKRIVELSE VARCHAR(200) NOT NULL;
ALTER TABLE BRUKER_REGISTRERING MODIFY KONSEPT_ID NUMBER NOT NULL;

ALTER TABLE BRUKER_REGISTRERING MODIFY ANDRE_UTFORDRINGER VARCHAR(30) NOT NULL;
ALTER TABLE BRUKER_REGISTRERING MODIFY BEGRUNNELSE_FOR_REGISTRERING varchar(30) NOT NULL;
ALTER TABLE BRUKER_REGISTRERING MODIFY UTDANNING_BESTATT VARCHAR(30) NOT NULL;
ALTER TABLE BRUKER_REGISTRERING MODIFY UTDANNING_GODKJENT_NORGE VARCHAR(30) NOT NULL;