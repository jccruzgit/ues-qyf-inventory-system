import facultyLogo from '../../assets/branding/logo-fqf.png';

function BrandMark({
  className = '',
  imageClassName = '',
  label = 'Facultad de Quimica y Farmacia',
}) {
  return (
    <div
      className={`relative flex h-12 w-12 items-center justify-center overflow-hidden rounded-2xl border border-white/75 bg-white/95 p-1.5 shadow-[0_18px_35px_rgba(23,61,44,0.18)] ${className}`}
    >
      <img
        src={facultyLogo}
        alt={label}
        className={`relative h-full w-full object-contain ${imageClassName}`}
      />
      <span className="sr-only">{label}</span>
    </div>
  );
}

export default BrandMark;
