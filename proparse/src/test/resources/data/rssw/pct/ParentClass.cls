ROUTINE-LEVEL ON ERROR UNDO, THROW.

USING Progress.Lang.*.

CLASS rssw.pct.ParentClass:
  DEFINE PROTECTED TEMP-TABLE tt1 NO-UNDO
    FIELD fld1 AS CHAR.

END CLASS.